package pokecube.core.moves;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.PartEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.events.pokemobs.combat.AttackEvent;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.moves.Battle;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.moves.damage.EntityMoveUse;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import pokecube.core.moves.damage.effects.Poison;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import thut.api.boom.ExplosionCustom;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class MovesUtils implements IMoveConstants
{
    public static enum AbleStatus
    {
        ABLE, SLEEP, FREEZE, AIOFF, GENERICUNABLE
    }

    public static Random rand = ThutCore.newRandom();

    public static Collection<String> getKnownMoveNames()
    {
        return MoveEntry.keys();
    }

    public static Collection<MoveEntry> getKnownMoves()
    {
        return MoveEntry.values();
    }

    public static void sendPairedMessages(final Entity target, final IPokemob attacker, final String baseKey,
            Object arg)
    {
        String key = baseKey + ".user";
        final IPokemob attacked = PokemobCaps.getPokemobFor(target);
        final Component targName = target != null
                ? target.getDisplayName()
                : attacker != null ? attacker.getDisplayName() : Component.literal("ERR PLS REPORT");
        Component msg =
                arg == null ? Component.translatableEscape(key, targName) : Component.translatableEscape(key, targName, arg);
        if (attacker != null) attacker.displayMessageToOwner(msg);
        key = baseKey + ".target";
        if (target != null && (attacker == null || target != attacker.getEntity()))
        {
            msg = arg == null ? Component.translatableEscape(key, targName) : Component.translatableEscape(key, targName, arg);
            if (attacked != null) attacked.displayMessageToOwner(msg);
        }
    }

    public static void sendPairedMessages(final Entity target, final IPokemob attacker, final String baseKey)
    {
        sendPairedMessages(target, attacker, baseKey, null);
    }

    /**
     * @return can attacker use its currently selected move.
     */
    public static boolean canUseMove(final IPokemob attacker)
    {
        if (MovesUtils.isAbleToUseMoves(attacker) != AbleStatus.ABLE) return false;
        if (attacker.getAttackCooldown() <= 0)
        {
            final int index = attacker.getMoveIndex();
            if (index < 4 && index >= 0) if (attacker.getDisableTimer(index) <= 0) return true;
            else
            {
                for (int i = 0; i < 4; i++) if (attacker.getDisableTimer(i) <= 0) return false;
                return true;
            }
            return true;
        }
        return false;
    }

    /**
     * For contact moves like tackle. The mob gets close to its target and hits.
     *
     * @return whether the mob can attack
     */
    public static boolean contactAttack(final IPokemob attacker, final Entity attacked)
    {
        if (attacked == null || attacker == null) return false;
        boolean inRange = false;
        final float dr = 0.5f;
        final Entity entity = attacker.getEntity();
        final PartEntity<?>[] parts = attacked.getParts();
        float attackerLength = attacker.getPokedexEntry().getLength() * attacker.getEntity().getScale() + dr;
        final float attackerHeight = attacker.getPokedexEntry().getHeight() * attacker.getEntity().getScale() + dr;
        float attackerWidth = attacker.getPokedexEntry().getHeight() * attacker.getEntity().getScale() + dr;
        attackerLength = Math.max(attackerLength, attackerHeight);
        attackerWidth = Math.max(attackerWidth, attackerHeight);
        attackerLength = Math.max(attackerLength, attackerWidth);
        attackerWidth = attackerLength;
        if (parts != null && parts.length > 0) for (final PartEntity<?> p : parts)
        {
            final float attackedLength = p.getBbWidth();
            final float attackedHeight = p.getBbHeight();
            final float attackedWidth = p.getBbWidth();

            final float dx = (float) (entity.getX() - p.getX());
            final float dz = (float) (entity.getZ() - p.getZ());
            final float dy = (float) (entity.getY() - p.getY());

            final AABB box = new AABB(0, 0, 0, attackerWidth, attackerHeight, attackerLength);
            final AABB box2 = new AABB(dx, dy, dz, dx + attackedWidth, dy + attackedHeight, dz + attackedLength);
            inRange = box.intersects(box2);
            if (inRange) break;
        }
        else
        {
            final float attackedLength = attacked.getBbWidth() + dr;
            final float attackedHeight = attacked.getBbHeight() + dr;
            final float attackedWidth = attacked.getBbWidth() + dr;

            final float dx = (float) (entity.getX() - attacked.getX());
            final float dz = (float) (entity.getZ() - attacked.getZ());
            final float dy = (float) (entity.getY() - attacked.getY());

            final AABB box = new AABB(0, 0, 0, attackerWidth, attackerHeight, attackerLength);
            final AABB box2 = new AABB(dx, dy, dz, dx + attackedWidth, dy + attackedHeight, dz + attackedLength);
            inRange = box.intersects(box2);
        }
        return inRange;
    }

    /**
     * @param efficiency    -1 = missed, -2 = failed, 0 = no effect, <1 = not effective, 1 = normal effecive, >1 =
     *                      supereffective
     * @param criticalRatio >1 = critical hit.
     */
    public static void displayEfficiencyMessages(final IPokemob attacker, final Entity attacked, final float efficiency,
            final float criticalRatio)
    {
        if (efficiency == -1)
        {
            MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.move.missed");
            return;
        }
        if (efficiency == -2)
        {
            MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.move.failed");
            return;
        }
        if (efficiency == 0)
        {
            MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.move.doesnt.affect");
            attacked.playSound(SoundEvents.PLAYER_ATTACK_NODAMAGE, 1, 1);
        }
        else if (efficiency < 1)
        {
            MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.move.not.very.effective");
            attacked.playSound(SoundEvents.PLAYER_ATTACK_WEAK, 1, 1);
        }
        else if (efficiency > 1)
        {
            MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.move.super.effective");
            attacked.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1, 1);
        }

        if (criticalRatio > 1)
        {
            MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.move.critical.hit");
            attacked.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1, 1);
        }
    }

    public static void displayMoveMessages(final IPokemob attacker, final Entity target, final String attack)
    {
        String baseKey = attack.equals(MoveEntry.CONFUSED.name) ? "pokemob.status.confusion" : "pokemob.move.used";
        MutableComponent otherArg = MovesUtils.getMoveName(attack, attacker);
        String key = baseKey + ".user";
        if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Move Message Send: {} on {}", baseKey, target);
        final IPokemob attacked = PokemobCaps.getPokemobFor(target);
        final Component targName = attacker != null ? attacker.getDisplayName() : target.getDisplayName();
        if (attacker != null) attacker.displayMessageToOwner(Component.translatableEscape(key, targName, otherArg));
        key = baseKey + ".target";
        if (target != attacker.getEntity() && target != null)
        {
            final Component message = Component.translatableEscape(key, targName, otherArg);
            if (attacked != null) attacked.displayMessageToOwner(message);
            else if (target instanceof Player player) PacketPokemobMessage.sendMessage(player, message);
        }
    }

    public static void displayStatsMessage(final IPokemob attacker, final Entity target, final float efficiency,
            final int stat, final byte amount)
    {
        if (efficiency == -2)
        {
            String baseKey = "pokemob.move.stat.fail";
            String key = baseKey + ".user";
            if (PokecubeCore.getConfig().debug_moves)
                PokecubeAPI.logInfo("Move Message Send: {} on {}", baseKey, target);
            final IPokemob attacked = PokemobCaps.getPokemobFor(target);
            final Component targName = attacker != null ? attacker.getDisplayName() : target.getDisplayName();
            if (attacker != null) attacker.displayMessageToOwner(Component.translatableEscape(key, targName));
            key = baseKey + ".target";
            if (target != attacker.getEntity() && target != null)
            {
                final Component message = Component.translatableEscape(key, targName);
                if (attacked != null) attacked.displayMessageToOwner(message);
                else if (target instanceof Player player) PacketPokemobMessage.sendMessage(player, message);
            }
        }
        else
        {
            String baseKey = "pokemob.move.stat";
            if (amount < 0) baseKey += ".fall" + -amount;
            else baseKey += ".rise" + amount;
            final String statName = "pokemob.move.stat" + stat;

            MutableComponent otherArg = Component.translatable(statName);
            String key = baseKey + ".user";
            if (PokecubeCore.getConfig().debug_moves)
                PokecubeAPI.logInfo("Move Message Send: {} on {}", baseKey, target);
            final IPokemob attacked = PokemobCaps.getPokemobFor(target);
            final Component targName = target != null
                    ? target.getDisplayName()
                    : attacker != null ? attacker.getDisplayName() : Component.literal("ERR PLS REPORT");
            if (attacker != null) attacker.displayMessageToOwner(Component.translatableEscape(key, targName, otherArg));
            key = baseKey + ".target";
            if (target != attacker.getEntity() && target != null)
            {
                final Component message = Component.translatableEscape(key, targName, otherArg);
                if (attacked != null) attacked.displayMessageToOwner(message);
                else if (target instanceof Player player) PacketPokemobMessage.sendMessage(player, message);
            }
        }
    }

    public static void displayStatusMessages(final IPokemob attacker, final Entity target, final int status,
            final boolean onMove)
    {
        final String baseKey = MovesUtils.getStatusMessage(status, onMove);
        if (baseKey != null)
        {
            String key = baseKey + ".user";
            final IPokemob attacked = PokemobCaps.getPokemobFor(target);
            final Component targName = target.getDisplayName();
            if (attacked != null) attacked.displayMessageToOwner(Component.translatableEscape(key, targName));
            key = baseKey + ".target";
            if (attacker != target)
            {
                final Component message = Component.translatableEscape(key, targName);
                if (attacker != null) attacker.displayMessageToOwner(message);
                else if (target instanceof Player player) PacketPokemobMessage.sendMessage(player, message);
            }
        }
    }

    public static MoveApplication doAttack(final String attackName, final IPokemob attacker,
            final LivingEntity attacked)
    {
        final MoveEntry move = MovesUtils.getMove(attackName);
        if (move != null) return move.applyMove(attacker, attacked, null);
        else
        {
            if (attackName != null) System.err.println("The Move \"" + attackName + "\" does not exist.");
            return MovesUtils.doAttack(IMoveConstants.DEFAULT_MOVE, attacker, attacked);
        }
    }

    public static int getAttackDelay(final IPokemob attacker, final String moveName, final boolean distanced,
            final boolean playerTarget)
    {
        int cd = PokecubeCore.getConfig().attackCooldown;
        if (playerTarget) cd *= 2;
        double accuracyMod = PokecubeAttributes.getModifierValue(attacker.getEntity(), Stats.ACCURACY);
        double moveMod = MovesUtils.getDelayMultiplier(attacker, moveName);

        int index = Stats.VIT.ordinal();
        double nat = (attacker.getNature().stats[index] * 10f + 100f) / 100f;
        int bs = attacker.getPokedexEntry().getStatVIT();
        int ev = attacker.getEVs()[index];
        int iv = attacker.getIVs()[index];
        double mod = PokecubeAttributes.getModifierValue(attacker.getEntity(), Stats.VIT);

        final double stat_based_cd = cd * (1 - nat * (bs / 100f + ev / 200f + iv / 50f + mod / 2f) / 10f);

        return (int) (stat_based_cd * moveMod / accuracyMod);
    }

    public static float getAttackStrength(final IPokemob attacker, final IPokemob attacked, final AttackCategory type,
            final int PWR, final MoveEntry move, float[] stat_mutliplier)
    {
        if (move.fixed) return move.getPWR(attacker, attacked.getEntity());

        if (PWR <= 0) return 0;

        var attackAttribute = attacker.getEntity().getAttribute(Attributes.ATTACK_DAMAGE);
        float statusMultiplier = attackAttribute.getBaseValue() > 0 ? (float) (attackAttribute.getValue()
                / attackAttribute.getBaseValue()) : 0f;

        final int level = attacker.getLevel();
        int ATT;
        int DEF;

        if (type == AttackCategory.SPECIAL)
        {
            ATT = (int) (attacker.getStat(Stats.SPATTACK, true) * stat_mutliplier[Stats.SPATTACK.ordinal()]);
            DEF = attacked.getStat(Stats.SPDEFENSE, true);
        }
        else
        {
            ATT = (int) (attacker.getStat(Stats.ATTACK, true) * stat_mutliplier[Stats.ATTACK.ordinal()]);
            DEF = attacked.getStat(Stats.DEFENSE, true);
        }

        // If this is a fight over a mate, the strength is reduced.
        if (attacker.getCombatState(CombatStates.MATEFIGHT) || attacked.getCombatState(CombatStates.MATEFIGHT))
            statusMultiplier *= 0.125f;

        ATT = (int) (statusMultiplier * ATT);

        return (level * 0.4F + 2F) * ATT * PWR / (DEF * 50F) + 2;
    }

    /**
     * Computes the delay between two moves in a fight from move and status effects.
     *
     * @return muliplier on attack delay
     */
    public static float getDelayMultiplier(final IPokemob attacker, final String moveName)
    {
        double moveCooldownFactor = PokecubeCore.getConfig().attackCooldown / 20f;

        var attackAttribute = attacker.getEntity().getAttribute(Attributes.ATTACK_SPEED);
        float statusMultiplier = attackAttribute.getBaseValue() > 0 ? (float) (attackAttribute.getValue()
                / attackAttribute.getBaseValue()) : 0f;

        moveCooldownFactor *= statusMultiplier;
        final MoveEntry move = MovesUtils.getMove(moveName);
        if (move == null) return 1;
        if (move.isContact(attacker))
        {
            moveCooldownFactor *= PokecubeCore.getConfig().attackCooldownContactScale;
        }
        if (move.isRanged(attacker))
        {
            moveCooldownFactor *= PokecubeCore.getConfig().attackCooldownRangedScale;
        }
        moveCooldownFactor *= move.getPostDelayFactor(attacker);
        return (float) moveCooldownFactor;
    }

    public static MoveEntry getMove(final String moveName)
    {
        if (moveName == null) return null;
        return MoveEntry.get(moveName);
    }

    public static MutableComponent getMoveName(final String attack, IPokemob user)
    {
        MoveEntry move = getMove(attack);
        MutableComponent name = Component.translatable("pokemob.move." + attack);
        if (move != null)
        {
            // Ternary statement prevents a crash where game doesn't know what type the move is
            name.setStyle(name.getStyle().withColor((move.getType(user) == null ? PokeType.unknown : move.getType(user)).colour));
        }
        return name;
    }

    protected static String getStatusMessage(final int status, final boolean onMove)
    {
        String message = null;
        if (status == IMoveConstants.STATUS_FRZ) message = "pokemob.move.isfrozen";
        if (status == IMoveConstants.STATUS_SLP) message = "pokemob.move.issleeping";
        if (status == IMoveConstants.STATUS_PAR && onMove) message = "pokemob.move.paralyzed";
        else if (status == IMoveConstants.STATUS_PAR) message = "pokemob.move.isfullyparalyzed";
        if (status == IMoveConstants.STATUS_BRN) message = "pokemob.move.isburned";
        if (status == IMoveConstants.STATUS_PSN) message = "pokemob.move.ispoisoned";
        if (status == IMoveConstants.STATUS_PSN2) message = "pokemob.move.isbadlypoisoned";
        return message;
    }

    public static float getTerrainDamageModifier(final PokeType type, final Entity attacker,
            final TerrainSegment terrain)
    {
        float ret = 1;
        final PokemobTerrainEffects effect = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemob_effects");
        if (type == PokeType.getType("dragon"))
            if (effect.isEffectActive(PokemobTerrainEffects.TerrainEffectType.MISTY)) ret = 0.5f;
        if (type == PokeType.getType("electric") && (attacker.onGround() || attacker.fallDistance < 0.5))
        {
            if (effect.isEffectActive(PokemobTerrainEffects.TerrainEffectType.ELECTRIC)) ret = 1.5f;

            if (effect.isEffectActive(PokemobTerrainEffects.TerrainEffectType.MUD)) ret *= 0.33f;
        }

        if (type == PokeType.getType("grass") && (attacker.onGround() || attacker.fallDistance < 0.5))
            if (effect.isEffectActive(PokemobTerrainEffects.TerrainEffectType.GRASS)) ret = 1.5f;

        if (type == PokeType.getType("water"))
        {
            if (effect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.RAIN)) ret = 1.5f;

            if (effect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN)) ret = 0.5f;
        }
        if (type == PokeType.getType("fire"))
        {
            if (effect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN)) ret = 1.5f;

            if (effect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.RAIN)) ret = 0.5f;

            if (effect.isEffectActive(PokemobTerrainEffects.TerrainEffectType.WATER)) ret *= 0.33f;
        }
        return ret;
    }

    public static String getUnlocalizedMove(final String attack)
    {
        return "pokemob.move." + attack;
    }

    public static record StatDiff(byte[] diffs, boolean applied)
    {}

    public static StatDiff handleStats(final IPokemob attacker, final Entity target, final int[] stats,
            final float chance)
    {
        IPokemob affected = PokemobCaps.getPokemobFor(target);
        float[] mods;
        mods = new float[Stats.values().length];
        // We start at 1, as there are not modifies for stat 0 (HP)
        for (int i = 1; i < mods.length; i++)
            if (chance > Math.random()) mods[i] = (byte) Math.max(-6, Math.min(6, mods[i] + stats[i]));
        byte[] diffs = new byte[stats.length];
        boolean ret = false;
        if (target instanceof LivingEntity targetLiving) for (int i = 0; i < stats.length; i++)
        {
            var stat = Stats.values()[i];
            int statEffect = stats[i];
            int oldValue = PokecubeAttributes.getModifier(targetLiving, stat);
            int newValue = Math.max(-6, Math.min(oldValue + statEffect, 6));
            if (oldValue != newValue)
            {
                PokecubeAttributes.applyStatModifier(targetLiving, stat, newValue);
                diffs[i] = (byte) (newValue - oldValue);
                ret = true;
            }
        }
        return new StatDiff(diffs, ret);
    }

    public static void sendStatDiffsMessages(final IPokemob attacker, final Entity target, StatDiff diffs)
    {
        if (diffs.applied)
        {
            for (byte i = 0; i < diffs.diffs.length; i++)
                if (diffs.diffs[i] != 0)
                {
                    MovesUtils.displayStatsMessage(attacker, target, 0, i, diffs.diffs[i]);
                }
        }
    }

    // Stat change occurs to targetPokemob from attacker.
    public static boolean handleStats2(final IPokemob targetPokemob, final Entity attacker, final int statEffect,
            final int statEffectAmount)
    {
        List<Stats> affected = new ArrayList<>();
        for (int i = 0; i < 8; i++)
        {
            boolean test = (statEffect & (1 << i)) > 0;
            if (test) affected.add(Stats.values()[i]);
        }
        boolean ret = false;
        for (var stat : affected)
        {
            int oldValue = PokecubeAttributes.getModifier(targetPokemob.getEntity(), stat);
            int newValue = Math.max(-6, Math.min(oldValue + statEffectAmount, 6));
            if (oldValue != newValue)
            {
                PokecubeAttributes.applyStatModifier(targetPokemob.getEntity(), stat, newValue);
                ret = true;
                MovesUtils.displayStatsMessage(targetPokemob, attacker, 0, stat.ordinal(),
                        (byte) (newValue - oldValue));
            }
        }
        return ret;
    }

    /**
     * @return is attacker able to use any moves, this doesn't check attack cooldown, instead checks things like status
     * or ai
     */
    public static AbleStatus isAbleToUseMoves(final IPokemob attacker)
    {
        if (!attacker.isRoutineEnabled(AIRoutine.AGRESSIVE)) return AbleStatus.AIOFF;
        if (attacker.getEntity().hasEffect(StatusEffects.SLEEP)) return AbleStatus.SLEEP;
        if (attacker.getEntity().hasEffect(StatusEffects.FREEZE)) return AbleStatus.FREEZE;
        return AbleStatus.ABLE;
    }

    public static boolean isMoveImplemented(String attackName)
    {
        if (attackName == null) return false;
        final MoveEntry move = getMove(attackName);
        return move != null;
    }

    /** creates an ExplosionCustom */
    public static ExplosionCustom newExplosion(final LivingEntity entity, final double x, final double y,
            final double z, final float power)
    {
        final ExplosionCustom var11 = new ExplosionCustom((ServerLevel) entity.level(), entity, x, y, z,
                power).setMaxRadius(PokecubeCore.getConfig().blastRadius);
        final IPokemob poke = PokemobCaps.getPokemobFor(entity);
        if (poke != null) if (poke.getOwner() instanceof Player) var11.owner = (Player) poke.getOwner();
        else var11.owner = null;
        return var11;
    }

    public static boolean setStatus(final IPokemob source, final LivingEntity attacked, int status)
    {
        final IPokemob attackedPokemob = PokemobCaps.getPokemobFor(attacked);

        var IDS = Lists.newArrayList(StatusEffects.EFFECT_BY_ID.keySet());
        List<Integer> ST = new ArrayList<>();
        boolean applied = false;

        // Prioritise bad posion if it exists, over regular
        if ((status & IMoveConstants.STATUS_PSN2) != 0) IDS.removeIf(i -> i == IMoveConstants.STATUS_PSN);
        else IDS.removeIf(i -> i == IMoveConstants.STATUS_PSN2);

        for (var s : IDS) if ((s & status) != 0) ST.add(s);

        final int start = ThutCore.newRandom().nextInt(1000);
        for (int i = 0; i < ST.size(); i++)
        {
            final int j = (i + start) % ST.size();
            status = ST.get(j);
            var effect = StatusEffects.getForId(status);
            if (attackedPokemob != null)
            {
                int amplifier = 1;
                // Set bad poison amplifier if applicable
                if (status == IMoveConstants.STATUS_PSN2) amplifier = Poison.BAD_POISON_AMPLIFIER;
                // 0 here means to apply a regular turn timer, 2-5 for statuses which heal themselves, -1 for permanent ones.
                final boolean apply = StatusEffects.setStatus(attacked, source.getEntity(), effect, 0, amplifier);
                if (apply) attackedPokemob.getEntity().getNavigation().stop();
                return true;
            }
            else if (attacked != null)
            {
                // 0 here means to apply a regular turn timer, 2-5 for statuses which heal themselves, -1 for permanent ones.
                final boolean apply = StatusEffects.setStatus(attacked, source.getEntity(), status, 0);
                applied = applied || apply;
            }
        }
        return applied;
    }

    public static void useMove(@Nonnull MoveEntry move, @Nonnull LivingEntity user, @Nullable LivingEntity target,
            @Nonnull final Vector3 start, @Nonnull final Vector3 end)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(user);
        if (pokemob == null) return;

        MoveApplication apply = new MoveApplication(move, pokemob, target);
        // Fire the AttackEvent first
        PokecubeAPI.MOVE_BUS.post(new AttackEvent(apply));
        // Pre-apply to run any special pre-processing needed for changing move
        // targets, etc.
        MoveApplicationRegistry.preApply(apply);
        move = apply.getMove();
        user = apply.getUserEntity();
        target = apply.getTarget();

        Predicate<MoveApplication> target_test = MoveApplicationRegistry.getValidator(move);
        LivingEntity mob = user;
        Level level = user.level;
        Battle battle = Battle.getBattle(user);

        Set<UUID> applied = new HashSet<>();

        boolean notUser = user != target;
        if (battle != null)
        {
            List<LivingEntity> targets = Lists.newArrayList();

            List<LivingEntity> options = Lists.newArrayList();
            // Actual target first.
            options.add(target);
            // Then targetted enemy
            options.add(pokemob.getMoveStats().targetEnemy);
            // Then targetted ally
            options.add(pokemob.getMoveStats().targetAlly);
            // Then all enemies
            options.addAll(battle.getEnemies(user));
            // Then all allies
            options.addAll(battle.getAllies(user));

            // Now ensure no null entries or duplicates in the actual list.
            for (var e : options) if (e != null && !targets.contains(e)) targets.add(e);

            // If we are in battle, lets deal with that here.
            for (var s : targets)
            {
                apply.setTarget(s);
                if (target_test.test(apply) && applied.add(s.getUUID()))
                {
                    if (PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Init(pokemob, move, s)).isCanceled())
                        continue;
                    // In this case, we had selected a new target from the
                    // battle, so we want to change our end for when the move is
                    // fired.
                    Vector3 use = new Vector3(end);
                    if (s != target) use.set(s);
                    final EntityMoveUse moveUse = EntityMoveUse.create(level, apply, use);
                    if (PokecubeCore.getConfig().debug_moves)
                        PokecubeAPI.logInfo("Queuing move: {} used by {} on {}", move.name,
                                user.getDisplayName().getString(), s.getDisplayName().getString());
                    MoveQueuer.queueMove(moveUse);
                    if (s == user || s == pokemob.getEntity())
                    {
                        notUser = false;
                    }
                    if (!move.isMultiTarget()) break;
                }
            }
        }
        else
        {
            // Otherwise manually check target and self, target first if not
            // null, then self, then finally use it on a location.
            boolean did = false;

            apply_test:
            {
                if (target != null)
                {
                    apply.setTarget(target);
                    if (target_test.test(apply) && applied.add(target.getUUID()))
                    {
                        if (PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Init(pokemob, move, target))
                                .isCanceled()) break apply_test;
                        final EntityMoveUse moveUse = EntityMoveUse.create(level, apply, end);
                        MoveQueuer.queueMove(moveUse);
                        did = true;
                        if (target == user)
                        {
                            notUser = false;
                        }
                    }
                }
                apply.setTarget(mob);
                if (target_test.test(apply) && applied.add(mob.getUUID()))
                {
                    if (PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Init(pokemob, move, target)).isCanceled())
                        break apply_test;
                    if (mob != target && target != null) end.set(mob);
                    final EntityMoveUse moveUse = EntityMoveUse.create(level, apply, end);
                    MoveQueuer.queueMove(moveUse);
                    did = true;
                    if (mob == user)
                    {
                        notUser = false;
                    }
                }

                if (!did)
                {
                    if (PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Init(pokemob, move, null)).isCanceled())
                        break apply_test;
                    apply.setTarget(null);
                    final EntityMoveUse moveUse = EntityMoveUse.create(level, apply, end);
                    MoveQueuer.queueMove(moveUse);
                }
            }
        }
        if (notUser)
        {
            apply.alreadyHit.add(user.getUUID());
        }
    }

    public static ItemStack applyEnchants(IPokemob pokemob, ItemStack tool)
    {
        ItemStack offhand = pokemob.getEntity().getOffhandItem();
        if (!offhand.isEmpty())
        {
            FakePlayer player = PokecubeMod.getFakePlayer(pokemob.getEntity().level());
            player.setExperienceLevels(1000);
            AnvilMenu menu = new AnvilMenu(0, player.getInventory());
            menu.getSlot(0).set(tool);
            menu.getSlot(1).set(offhand.copy());
            menu.createResult();
            return menu.getSlot(2).getItem();
        }
        return tool;
    }

    public static void harvestBlock(IPokemob pokemob, ItemStack tool, BlockState state, BlockPos pos, Level worldIn,
            ServerPlayer player, boolean applyEnchants)
    {
        if (applyEnchants) tool = applyEnchants(pokemob, tool);
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        worldIn.destroyBlock(pos, false);
        if (player == null) Block.dropResources(state, worldIn, pos, blockEntity, player, tool);
        else state.getBlock().playerDestroy(worldIn, player, pos, state, blockEntity, tool);
    }
}
