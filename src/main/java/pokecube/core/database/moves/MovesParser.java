package pokecube.core.database.moves;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.moves.MoveEntry.Category;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;

public class MovesParser
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

    private static void addCategory(final byte mask, final MoveEntry move)
    {
        if ((move.attackCategory & mask) == 0) move.attackCategory += mask;
    }

    private static void addChange(final byte mask, final MoveEntry move)
    {
        if ((move.change & mask) == 0) move.change += mask;
    }

    private static void addStatus(final byte mask, final MoveEntry move)
    {
        if ((move.statusChange & mask) == 0) move.statusChange += mask;
    }

    @Nullable
    static String getMatch(final String input, final Pattern pattern)
    {
        final Matcher match = pattern.matcher(input);
        if (match.find()) return match.group();
        return null;
    }

    private static int getRate(String chance)
    {
        if (chance == null) chance = "100";
        int rate;
        try
        {
            rate = Integer.parseInt(MovesParser.getMatch(chance, MovesParser.NUMBER));
        }
        catch (final NumberFormatException e)
        {
            rate = 100;
        }
        return rate;
    }

    public static void initMoveEntry(final MoveJsonEntry entry, final int index)
    {
        final String name = Database.convertMoveName(entry.name);
        int power;
        int pp;
        int accuracy;
        try
        {
            power = Integer.parseInt(entry.pwr);
            pp = Integer.parseInt(entry.pp);
            accuracy = Integer.parseInt(entry.acc);
        }
        catch (final NumberFormatException e)
        {
            PokecubeCore.LOGGER.error("Error with " + entry.readableName, e);
            return;
        }
        final String yes = "Yes";
        MoveEntry move = MoveEntry.get(name);
        move = move == null ? new MoveEntry(name, index) : move;
        move.attackCategory = 0;
        move.power = power;
        move.pp = pp;
        move.accuracy = accuracy;
        move.baseEntry = entry;
        move.cooldown_scale = entry.cooldown_scale;
        final boolean contact = yes.equals(entry.contact);
        final boolean sound = yes.equals(entry.soundType);
        final boolean punch = yes.equals(entry.punchType);
        final boolean snatch = yes.equals(entry.snatchable);
        final boolean magiccoat = yes.equals(entry.magiccoat);
        final boolean defrosts = yes.equals(entry.defrosts);
        final boolean protect = yes.equals(entry.protect);
        final boolean mirror = yes.equals(entry.mirrormove);
        // TODO decide what to do with these.
        // boolean wideArea = yes.equals(entry.wideArea);
        // boolean zMove = yes.equals(entry.zMove);
        move.defrosts = defrosts;
        move.mirrorcoated = mirror;
        MovesParser.addCategory(contact ? IMoveConstants.CATEGORY_CONTACT : IMoveConstants.CATEGORY_DISTANCE, move);
        move.soundType = sound;
        move.isPunch = punch;
        move.snatch = snatch;
        move.magiccoat = magiccoat;
        move.protect = protect;
        move.type = MovesParser.parseType(entry.type);
        if (entry.defaultanimation != null) move.animDefault = entry.defaultanimation;
        if (entry.effectRate == null) entry.effectRate = "100";
        if (entry.secondaryEffect != null) MovesParser.parseSecondaryEffects(entry, move);
        MovesParser.parseCategory(entry.category, move);
        MovesParser.parseTarget(entry, move);
        MovesParser.parseStatusEffects(entry, move);
        MovesParser.parseStatModifiers(entry, move);
        MovesParser.parseFixedDamage(entry, move);
        MovesParser.parseHealing(entry, move);
        MovesParser.parseSelfDamage(entry, move);
        MovesParser.parsePreset(entry);
        MovesParser.parseSize(entry, move);
    }

    private static void parseSize(final MoveJsonEntry entry, final MoveEntry move)
    {
        if (entry.customSize != null)
        {
            final String[] args = entry.customSize.split(",");

            float sh;
            float sv;
            if (args.length == 1)
            {
                sv = sh = 0;
                try
                {
                    sh = Float.valueOf(args[0]);
                }
                catch (final NumberFormatException e)
                {
                    PokecubeCore.LOGGER.error("Error in with move size for {}", entry.readableName);
                    return;
                }
                sv = sh;
            }
            else if (args.length == 2)
            {
                sv = sh = 0;
                try
                {
                    sh = Float.valueOf(args[0]);
                    sv = Float.valueOf(args[1]);
                }
                catch (final NumberFormatException e)
                {
                    PokecubeCore.LOGGER.error("Error in with move size for {}", entry.readableName);
                    return;
                }
            }
            else
            {
                PokecubeCore.LOGGER.error("Error in with move size for {}, must be 1 or 2 numbers separated by ,",
                        entry.readableName);
                return;
            }
            move.customSize = new float[]
            { sh, sv, sh };
        }
    }

    public static void load(final MovesJson moves) throws IOException
    {
        for (int i = 0; i < moves.moves.size(); i++)
        {
            final MoveJsonEntry entry = moves.moves.get(i);
            try
            {
                MovesParser.initMoveEntry(entry, i + 1);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error in move " + entry.readableName, e);
            }
        }
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

    static void parseCategory(final String category, final MoveEntry move)
    {
        final String other = "Other";
        final String special = "Special";
        final String physical = "Physical";
        move.category = 0;
        if (other.equals(category)) move.category = (byte) Category.OTHER.ordinal();
        if (special.equals(category)) move.category = (byte) Category.SPECIAL.ordinal();
        if (physical.equals(category)) move.category = (byte) Category.PHYSICAL.ordinal();
    }

    private static void parseCrit(final String details, final MoveEntry move)
    {
        final boolean crit = details != null && (details.contains("has an increased critical hit ratio")
                || details.contains("has high critical hit ratio"));
        final boolean alwaysCrit = details != null && details.contains("always inflicts a critical hit");

        if (alwaysCrit)
        {
            move.crit = 255;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(move.name + " set to always crit.");
        }
        else if (crit)
        {
            move.crit = 2;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(move.name + " set to twice crit rate.");
        }
        else move.crit = 1;
    }

    private static void parseFixedDamage(final MoveJsonEntry entry, final MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        if (entry.secondaryEffect.toLowerCase(Locale.ENGLISH).equalsIgnoreCase("may cause one-hit ko."))
            entry.ohko = true;
        String var = entry.secondaryEffect.toLowerCase(Locale.ENGLISH).trim();
        final boolean fixed = var.contains("inflicts") && var.contains("hp damage.");
        if (fixed)
        {
            var = MovesParser.getMatch(var, MovesParser.NUMBER);
            move.fixed = true;
            try
            {
                move.power = Integer.parseInt(var);
                if (PokecubeMod.debug) PokecubeCore.LOGGER.info(entry.readableName + " set to fixed damage of " + var);
            }
            catch (final NumberFormatException e)
            {
                PokecubeCore.LOGGER.error("Error parsing fixed damage for " + entry.readableName + " "
                        + entry.secondaryEffect + " " + var, e);
            }
        }
    }

    private static void parseHealing(final MoveJsonEntry entry, final MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        String var = entry.secondaryEffect.toLowerCase(Locale.ENGLISH).trim();
        if (entry.battleEffect != null) var = var + "|" + entry.battleEffect.toLowerCase(Locale.ENGLISH).trim();
        final boolean ratioHeal = var.contains("user recovers") && var.contains(" the damage inflicted");
        final boolean healRatio = var.contains("user recovers") && var.contains(" the maximum hp");
        final boolean healOther = MovesParser.HEALOTHER.matcher(var).find();
        if (ratioHeal)
        {
            final Matcher number = MovesParser.NUMBER.matcher(var);
            final Matcher third = MovesParser.THIRD.matcher(var);
            final Matcher half = MovesParser.HALF.matcher(var);
            if (number.find()) move.damageHeal = Integer.parseInt(number.group()) / 100f;
            else if (half.find()) move.damageHeal = 0.5f;
            else if (third.find()) move.damageHeal = 1 / 3f;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(move.name + " set to damage heal of " + move.damageHeal);
            return;
        }
        else if (healRatio)
        {
            final Matcher number = MovesParser.NUMBER.matcher(var);
            final Matcher third = MovesParser.THIRD.matcher(var);
            final Matcher half = MovesParser.HALF.matcher(var);
            if (number.find()) move.selfHealRatio = Integer.parseInt(number.group()) / 100f;
            else if (half.find()) move.selfHealRatio = 0.5f;
            else if (third.find()) move.selfHealRatio = 1 / 3f;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(move.name + " set to self heal of " + move.damageHeal);
            return;
        }
        else if (healOther)
        {
            final Matcher half = MovesParser.HALF.matcher(var);
            if (half.find()) move.targetHealRatio = 0.5f;
            else move.targetHealRatio = 0.2f;
            // TODO fairy one heals more on grassy terrain, maybe that needs
            // custom logic?
        }
        if (var.contains("user restores health"))
        {
            move.selfHealRatio = 1;
            return;
        }
    }

    static void parseNoMove(final String secondaryEffect, final MoveEntry move)
    {
        if (secondaryEffect.equals("User cannot Attack on the next turn.") && move.cooldown_scale == 1)
        {
            move.cooldown_scale = 4.0f;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(move.name + " set as long cooldown move.");
        }
    }

    private static void parsePreset(final MoveJsonEntry entry)
    {
        if (entry.secondaryEffect != null && entry.secondaryEffect.startsWith("Traps")) entry.preset = "ongoing";
    }

    static void parseSecondaryEffects(final MoveJsonEntry entry, final MoveEntry move)
    {
        MovesParser.parseNoMove(entry.secondaryEffect, move);
        MovesParser.parseCrit(entry.secondaryEffect.toLowerCase(Locale.ENGLISH), move);
        if (entry.secondaryEffect.contains("Cannot miss.")
                || entry.battleEffect != null && entry.battleEffect.contains("never misses"))
            entry.interceptable = false;
    }

    private static void parseSelfDamage(final MoveJsonEntry entry, final MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        final String var = entry.secondaryEffect.toLowerCase(Locale.ENGLISH).trim();
        final boolean recoils = var.contains("user takes recoil damage equal to ")
                && var.contains(" of the damage inflicted.");
        if (recoils)
        {
            final Matcher number = MovesParser.NUMBER.matcher(var);
            final Matcher third = MovesParser.THIRD.matcher(var);
            final Matcher half = MovesParser.HALF.matcher(var);
            float damage = 0;
            if (number.find()) damage = Integer.parseInt(number.group()) / 100f;
            else if (third.find()) damage = 1 / 3f;
            else if (half.find()) damage = 1 / 2f;
            move.selfDamage = damage;
            move.selfDamageType = MoveEntry.DAMAGEDEALT;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(move.name + " set to recoil factor of " + damage);
            return;
        }
        final boolean userFaint = var.contains("user faints");
        if (userFaint)
        {
            move.selfDamage = 10000;
            move.selfDamageType = MoveEntry.TOTALHP;
            return;
        }
    }

    private static void parseStatModifiers(final MoveJsonEntry entry, final MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        final String[] effects = entry.secondaryEffect.split("\\.");
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
            final int rate = MovesParser.getRate(entry.effectRate);
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

    private static void parseStatusEffects(final MoveJsonEntry entry, final MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        final String effect = entry.secondaryEffect.toLowerCase(Locale.ENGLISH);
        final boolean burn = MovesParser.matches(effect, MovesParser.BRNA, MovesParser.BRNB);
        final boolean par = MovesParser.matches(effect, MovesParser.PARA, MovesParser.PARB);
        final boolean poison = MovesParser.matches(effect, MovesParser.PSNA, MovesParser.PSNB);
        final boolean frz = MovesParser.matches(effect, MovesParser.FRZA, MovesParser.FRZB);
        final boolean slp = MovesParser.matches(effect, MovesParser.SLPA, MovesParser.SLPB);
        final boolean poison2 = MovesParser.matches(effect, MovesParser.PSNC);
        final boolean confuse = effect.contains("confus");
        final boolean flinch = effect.contains("flinch");
        if (burn) MovesParser.addStatus(IMoveConstants.STATUS_BRN, move);
        if (par) MovesParser.addStatus(IMoveConstants.STATUS_PAR, move);
        if (frz) MovesParser.addStatus(IMoveConstants.STATUS_FRZ, move);
        if (slp) MovesParser.addStatus(IMoveConstants.STATUS_SLP, move);
        if (poison) MovesParser.addStatus(poison2 ? IMoveConstants.STATUS_PSN2 : IMoveConstants.STATUS_PSN, move);
        if (confuse) MovesParser.addChange(IMoveConstants.CHANGE_CONFUSED, move);
        if (flinch) MovesParser.addChange(IMoveConstants.CHANGE_FLINCH, move);
        final int rate = MovesParser.getRate(entry.effectRate);
        if (confuse || flinch) move.chanceChance = rate / 100f;
        move.statusChance = rate / 100f;
        if (slp || burn || par || poison || frz || slp) if (PokecubeMod.debug)
            PokecubeCore.LOGGER.info(move.name + " Has Status Effects: " + move.statusChange + " " + move.statusChance);
    }

    private static void parseTarget(final MoveJsonEntry entry, final MoveEntry move)
    {
        final String target = entry.target;
        final String self = "Self";
        if (self.equals(target) && (move.attackCategory & IMoveConstants.CATEGORY_SELF) == 0)
            move.attackCategory += IMoveConstants.CATEGORY_SELF;
        if (target != null && target.contains("All Adjacent")) entry.multiTarget = true;
        if (target != null && target.equals("All"))
        {
            entry.multiTarget = true;
            entry.wideArea = "Yes";
        }
    }

    private static PokeType parseType(final String type)
    {
        return PokeType.getType(type);
    }

}
