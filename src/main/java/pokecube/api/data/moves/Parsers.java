package pokecube.api.data.moves;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.MoveEntry.Category;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.core.impl.PokecubeMod;

public class Parsers
{
    static final Pattern NUMBER = Pattern.compile("([0-9])+");
    static final Pattern HALF = Pattern.compile("half");
    static final Pattern THIRD = Pattern.compile("third");

    static final Pattern PSNA = Pattern.compile("(induce).*(poison)");
    static final Pattern PSNB = Pattern.compile("(may).*(poison)");
    static final Pattern PSNC = Pattern.compile("(induce).*(severe).*(poison)");

    static final Pattern PARA = Pattern.compile("(induce).*(paralysis)");
    static final Pattern PARB = Pattern.compile("(may).*(paralyze)");

    static final Pattern BRNA = Pattern.compile("(induce).*(burn)");
    static final Pattern BRNB = Pattern.compile("(may).*(burn)");

    static final Pattern FRZA = Pattern.compile("(induce).*(freeze)");
    static final Pattern FRZB = Pattern.compile("(may).*(freeze)");

    static final Pattern SLPA = Pattern.compile("(induce).*(sleep)");
    static final Pattern SLPB = Pattern.compile("(may).*(sleep)");

    static final Pattern HEALOTHER = Pattern.compile("(restores the target's hp)");

    @Nullable
    static String getMatch(final String input, final Pattern pattern)
    {
        final Matcher match = pattern.matcher(input);
        if (match.find()) return match.group();
        return null;
    }

    static boolean matches(final String input, final Pattern... patterns)
    {
        for (final Pattern pattern : patterns)
        {
            final Matcher match = pattern.matcher(input);
            if (match.find()) return true;
        }
        return false;
    }

    public static abstract class BaseParser
    {
        void addCategory(final byte mask, final MoveEntry move)
        {
            if ((move.attackCategory & mask) == 0) move.attackCategory += mask;
        }

        void addChange(final int mask, final MoveEntry move)
        {
            if ((move.change & mask) == 0) move.change += mask;
        }

        void addStatus(final int mask, final MoveEntry move)
        {
            if ((move.statusChange & mask) == 0) move.statusChange += mask;
        }

        void parseCategory(final String category, final MoveEntry move)
        {
            final String other = "status";
            final String special = "special";
            final String physical = "physical";
            move.category = 0;
            if (other.equals(category)) move.category = (byte) Category.OTHER.ordinal();
            if (special.equals(category)) move.category = (byte) Category.SPECIAL.ordinal();
            if (physical.equals(category)) move.category = (byte) Category.PHYSICAL.ordinal();
        }

        void parseStatModifiers(String text, final MoveEntry move, int rate)
        {
            if (text.isBlank()) return;
            final String[] effects = text.split("\\.");
            for (final String s : effects)
            {
                final String effect = s.toLowerCase(Locale.ENGLISH).trim();
                if (s.isEmpty()) continue;
                final boolean lower = effect.contains("lower");
                final boolean raise = effect.contains("raise") || effect.contains("boost");
                final boolean user = effect.contains("user") && !effect.contains("opponent's");
                boolean atk = effect.contains("attack");
                final boolean spatk = effect.contains("special attack");
                boolean def = effect.contains("defense");
                final boolean spdef = effect.contains("special defense");
                final boolean speed = effect.contains("speed");
                final boolean acc = effect.contains("accuracy");
                final boolean evas = effect.contains("evasion");
                int stages = 1;
                if (effect.contains("two stage") || effect.contains("2 stage")) stages = 2;
                if (effect.contains("three stage") || effect.contains("3 stage")) stages = 3;
                if (lower) stages *= -1;
                else if (!raise) stages = 0;
                if (!(raise || lower)) continue;

                if (atk && spatk) // check to ensure is both;
                    atk = effect.replaceFirst("attack", "").contains("attack");
                if (def && spdef) // check to ensure is both;
                    def = effect.replaceFirst("defense", "").contains("defense");
                int[] amounts = null;
                if (user)
                {
                    move.attackerStatModProb = rate / 100f;
                    amounts = move.attackerStatModification;
                }
                else
                {
                    move.attackedStatModProb = rate / 100f;
                    amounts = move.attackedStatModification;
                }
                if (atk) amounts[Stats.ATTACK.ordinal()] = stages;
                if (def) amounts[Stats.DEFENSE.ordinal()] = stages;
                if (spatk) amounts[Stats.SPATTACK.ordinal()] = stages;
                if (spdef) amounts[Stats.SPDEFENSE.ordinal()] = stages;
                if (speed) amounts[Stats.VIT.ordinal()] = stages;
                if (acc) amounts[Stats.ACCURACY.ordinal()] = stages;
                if (evas) amounts[Stats.EVASION.ordinal()] = stages;
            }
        }

        void parseStatusEffects(final String effectText, final MoveEntry move, int rate)
        {
            if (effectText.isBlank()) return;
            final boolean burn = Parsers.matches(effectText, Parsers.BRNA, Parsers.BRNB);
            final boolean par = Parsers.matches(effectText, Parsers.PARA, Parsers.PARB);
            final boolean poison = Parsers.matches(effectText, Parsers.PSNA, Parsers.PSNB);
            final boolean frz = Parsers.matches(effectText, Parsers.FRZA, Parsers.FRZB);
            final boolean slp = Parsers.matches(effectText, Parsers.SLPA, Parsers.SLPB);
            final boolean poison2 = Parsers.matches(effectText, Parsers.PSNC);
            final boolean confuse = effectText.contains("confus");
            final boolean flinch = effectText.contains("flinch");
            if (burn) addStatus(IMoveConstants.STATUS_BRN, move);
            if (par) addStatus(IMoveConstants.STATUS_PAR, move);
            if (frz) addStatus(IMoveConstants.STATUS_FRZ, move);
            if (slp) addStatus(IMoveConstants.STATUS_SLP, move);
            if (poison) addStatus(poison2 ? IMoveConstants.STATUS_PSN2 : IMoveConstants.STATUS_PSN, move);
            if (confuse) addChange(IMoveConstants.CHANGE_CONFUSED, move);
            if (flinch) addChange(IMoveConstants.CHANGE_FLINCH, move);
            if (confuse || flinch) move.chanceChance = rate / 100f;
            move.statusChance = rate / 100f;
            if (slp || burn || par || poison || frz || slp) if (PokecubeMod.debug) PokecubeAPI.LOGGER
                    .info(move.name + " Has Status Effects: " + move.statusChange + " " + move.statusChance);
        }

        public void process(MoveEntry entry)
        {
            entry.root_entry.preParse();
            entry.type = PokeType.getType(entry.root_entry.getMove().type);
            entry.power = entry.root_entry.getMove().power;
            entry.pp = entry.root_entry.getMove().pp;
            entry.accuracy = entry.root_entry.getMove().accuracy;
            entry.crit = entry.root_entry.getMove().crit_rate;

            parseCategory(entry.root_entry.getMove().damage_class, entry);
            entry.attackCategory = 0;
        }
    }

    public static class DamageParser extends BaseParser
    {
        @Override
        public void process(MoveEntry entry)
        {
            // Process parent stuff first.
            super.process(entry);

            // Now lets see if we need to do any special stuff.

            // First check if we do fixed damage.
            if (entry.root_entry._effect_text_simple.contains("points of damage."))
            {
                String var = Parsers.getMatch(entry.root_entry._effect_text_simple, Parsers.NUMBER);
                if (var != null)
                {
                    try
                    {
                        entry.fixed = true;
                        entry.power = Integer.parseInt(var);
                        if (PokecubeMod.debug) PokecubeAPI.LOGGER.info(entry.name + " set to fixed damage of " + var);
                        return;
                    }
                    catch (final NumberFormatException e)
                    {
                        PokecubeAPI.LOGGER.error("Error parsing fixed damage for " + entry.name + " "
                                + entry.root_entry._effect_text_simple + " " + var, e);
                    }
                }
            }
        }
    }

    public static class AilmentParser extends BaseParser
    {
        @Override
        public void process(MoveEntry entry)
        {
            super.process(entry);
            int rate = 100;
            parseStatusEffects(entry.root_entry._effect_text_simple, entry, rate);
        }
    }

    public static class NetGoodStatsParser extends BaseParser
    {
        @Override
        public void process(MoveEntry entry)
        {
            super.process(entry);

            int rate = 100;
            parseStatModifiers(entry.root_entry._effect_text_simple, entry, rate);
        }
    }

    public static class HealParser extends BaseParser
    {

    }

    public static class DamageAlimentParser extends DamageParser
    {

    }

    public static class SwaggerParser extends BaseParser
    {

    }

    public static class DamageLowerParser extends DamageParser
    {

    }

    public static class DamageRaiseParser extends DamageParser
    {

    }

    public static class DamageHealParser extends DamageParser
    {

    }

    public static class OHKOParser extends BaseParser
    {

    }

    public static class WholeFieldEffectParser extends BaseParser
    {

    }

    public static class FieldEffectParser extends BaseParser
    {

    }

    public static class ForceSwitchParser extends BaseParser
    {

    }

    public static class UniqueParser extends BaseParser
    {

    }

    private static final Map<String, BaseParser> PARSERS = Maps.newHashMap();

    static
    {
        PARSERS.put("damage", new DamageParser());
        PARSERS.put("ailment", new AilmentParser());
        PARSERS.put("net-good-stats", new NetGoodStatsParser());
        PARSERS.put("heal", new HealParser());
        PARSERS.put("damage+ailment", new DamageAlimentParser());
        PARSERS.put("swagger", new SwaggerParser());
        PARSERS.put("damage+lower", new DamageLowerParser());
        PARSERS.put("damage+raise", new DamageRaiseParser());
        PARSERS.put("damage+heal", new DamageHealParser());
        PARSERS.put("ohko", new OHKOParser());
        PARSERS.put("whole-field-effect", new WholeFieldEffectParser());
        PARSERS.put("field-effect", new FieldEffectParser());
        PARSERS.put("force-switch", new ForceSwitchParser());
        PARSERS.put("unique", new UniqueParser());
    }

    public static BaseParser getParser(String category)
    {
        return PARSERS.get(category);
    }
}
