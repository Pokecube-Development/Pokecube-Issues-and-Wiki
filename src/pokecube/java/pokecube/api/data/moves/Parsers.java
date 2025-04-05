package pokecube.api.data.moves;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.IMoveConstants.AttackCategory;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;

public class Parsers
{
    static final Pattern NUMBER = Pattern.compile("([0-9])+");
    static final Pattern HALF = Pattern.compile("half");
    static final Pattern THIRD = Pattern.compile("third");

    static final List<Pattern> PSN2_PATTERNS = Lists.newArrayList(Pattern.compile("(badly poisons the target)"));
    static final List<Pattern> PSN_PATTERNS = Lists.newArrayList(Pattern.compile("(poisons the target)"),
            Pattern.compile("(chance to poison the target)"));
    static final List<Pattern> PAR_PATTERNS = Lists.newArrayList(Pattern.compile("(paralyzes the target)"),
            Pattern.compile("(chance to paralyze the target)"));
    static final List<Pattern> BRN_PATTERNS = Lists.newArrayList(Pattern.compile("(burns the target)"),
            Pattern.compile("(chance to burn the target)"));
    static final List<Pattern> FRZ_PATTERNS = Lists.newArrayList(Pattern.compile("(chance to freeze the target)"));
    static final List<Pattern> SLP_PATTERNS = Lists.newArrayList(Pattern.compile("(puts the target to sleep)"),
            Pattern.compile("(induce).*(sleep)"), Pattern.compile("(may).*(sleep)"));

    static final List<Pattern> OHKO = Lists.newArrayList(Pattern.compile("(causes a one-hit ko.)"));

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
        void addStatus(final int mask, final MoveEntry move)
        {
            if ((move.root_entry._status_effects & mask) == 0) move.root_entry._status_effects += mask;
        }

        void parseCategory(final MoveEntry move)
        {
            final String other = "status";
            final String special = "special";
            final String physical = "physical";
            move.category = AttackCategory.OTHER;

            // Process move attack category
            switch (move.root_entry.getMove().damage_class)
            {
            case other:
                move.category = AttackCategory.STATUS;
                break;
            case special:
                move.category = AttackCategory.SPECIAL;
                break;
            case physical:
                move.category = AttackCategory.PHYSICAL;
                break;
            }

            // Process whether move is "aoe"
            switch (move.root_entry._target_type)
            {
            case "all-opponents", "all-pokemon", "all-other-pokemon", "user-and-allies", "all-allies", "entire-field", "opponents-field", "users-field":
                move.root_entry._aoe = true;
                break;
            }
            // Process if move targets multiples.
            switch (move.root_entry._target_type)
            {
            case "all-opponents", "all-pokemon", "all-other-pokemon", "user-and-allies", "all-allies":
                move.root_entry._multi_target = true;
                break;
            }
        }

        void parseStatModifiers(String effect, final MoveEntry move, int rate)
        {
            if (effect.isBlank()) return;
            boolean lower = effect.contains("lower");
            boolean raise = effect.contains("raise") || effect.contains("boost");
            boolean atk = effect.contains("attack");
            boolean spatk = effect.contains("special attack");
            boolean def = effect.contains("defense");
            boolean spdef = effect.contains("special defense");
            boolean speed = effect.contains("speed");
            boolean acc = effect.contains("accuracy");
            boolean evas = effect.contains("evasion");
            int stages = 1;
            if (effect.contains("one stage") || effect.contains("1 stage")) stages = 1;
            if (effect.contains("two stage") || effect.contains("2 stage")) stages = 2;
            if (effect.contains("three stage") || effect.contains("3 stage")) stages = 3;
            if (lower) stages *= -1;
            else if (!raise) stages = 0;
            if (!(raise || lower)) return;
            if (atk && spatk) // check to ensure is both;
                atk = effect.replaceFirst("attack", "").contains("attack");
            if (def && spdef) // check to ensure is both;
                def = effect.replaceFirst("defense", "").contains("defense");
            if (!(atk || def || spatk || spdef || speed || acc || evas)) return;
            int[] amounts = move.root_entry._stat_effects;
            move.root_entry._stat_chance = rate / 100.0f;
            if (atk) amounts[Stats.ATTACK.ordinal()] = stages;
            if (def) amounts[Stats.DEFENSE.ordinal()] = stages;
            if (spatk) amounts[Stats.SPATTACK.ordinal()] = stages;
            if (spdef) amounts[Stats.SPDEFENSE.ordinal()] = stages;
            if (speed) amounts[Stats.VIT.ordinal()] = stages;
            if (acc) amounts[Stats.ACCURACY.ordinal()] = stages;
            if (evas) amounts[Stats.EVASION.ordinal()] = stages;
        }

        void parseStatusEffects(final String effectText, final MoveEntry move, int rate)
        {
            if (effectText.isBlank()) return;
            final boolean burn = BRN_PATTERNS.stream().anyMatch(p -> Parsers.matches(effectText, p));
            final boolean par = PAR_PATTERNS.stream().anyMatch(p -> Parsers.matches(effectText, p));
            final boolean poison = PSN_PATTERNS.stream().anyMatch(p -> Parsers.matches(effectText, p));
            final boolean frz = FRZ_PATTERNS.stream().anyMatch(p -> Parsers.matches(effectText, p));
            final boolean slp = SLP_PATTERNS.stream().anyMatch(p -> Parsers.matches(effectText, p));
            final boolean poison2 = PSN2_PATTERNS.stream().anyMatch(p -> Parsers.matches(effectText, p));
            final boolean confuse = effectText.contains("confus");
            final boolean flinch = effectText.contains("flinch");
            if (burn) addStatus(IMoveConstants.STATUS_BRN, move);
            if (par) addStatus(IMoveConstants.STATUS_PAR, move);
            if (frz) addStatus(IMoveConstants.STATUS_FRZ, move);
            if (slp) addStatus(IMoveConstants.STATUS_SLP, move);
            if (poison) addStatus(poison2 ? IMoveConstants.STATUS_PSN2 : IMoveConstants.STATUS_PSN, move);
            if (confuse) addStatus(IMoveConstants.CHANGE_CONFUSED, move);
            if (flinch) addStatus(IMoveConstants.CHANGE_FLINCH, move);
            move.root_entry._status_chance = rate / 100f;
        }

        public void process(MoveEntry entry)
        {
            entry.root_entry.preParse();
            entry.type = PokeType.getType(entry.root_entry.getMove().type);
            entry.power = entry.root_entry.getMove().power;
            entry.pp = entry.root_entry.getMove().pp;
            entry.accuracy = entry.root_entry.getMove().accuracy;
            entry.crit = entry.root_entry.getMove().crit_rate;

            entry.root_entry._ohko = OHKO.stream()
                    .anyMatch(p -> Parsers.matches(entry.root_entry._effect_text_simple, p));

            parseCategory(entry);
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
                        if (PokecubeCore.getConfig().debug_moves)
                            PokecubeAPI.logInfo(entry.name + " set to fixed damage of " + var);
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
        @Override
        public void process(MoveEntry entry)
        {
            super.process(entry);
            MoveApplicationRegistry.registerAllyTargetMove(entry.name);
        }
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

    private static final Map<String, BaseParser> CUSTOM_PARSERS = Maps.newHashMap();

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

    /**
     * This is used to register a custom parser for moves. Call this during
     * InitDatabase.Pre, as that is send right before the custom parsers would
     * be first used.
     * 
     * @param move
     * @param parser
     */
    public static void registerCustomParser(String move, BaseParser parser)
    {
        CUSTOM_PARSERS.put(move, parser);
    }

    public static BaseParser getParser(String category)
    {
        return PARSERS.get(category);
    }

    public static BaseParser getCustomParser(String move)
    {
        return CUSTOM_PARSERS.get(move);
    }
}
