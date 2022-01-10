package pokecube.core.ai.brain;

import java.util.List;
import java.util.Optional;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.events.SetAttackTargetEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.utils.AITools;
import thut.api.entity.ai.BrainUtil;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.maths.Vector3;

public class BrainUtils extends BrainUtil
{
    public static LivingEntity getAttackTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemoryValue(MemoryModules.ATTACKTARGET)) return brain.getMemory(MemoryModules.ATTACKTARGET).get();
        else if (mobIn instanceof Mob) return ((Mob) mobIn).getTarget();
        else return null;
    }

    public static boolean hasAttackTarget(final LivingEntity mobIn)
    {
        return BrainUtils.getAttackTarget(mobIn) != null;
    }

    public static void clearAttackTarget(final LivingEntity mobIn)
    {
        BrainUtils.setAttackTarget(mobIn, null);
    }

    public static void setAttackTarget(final LivingEntity mobIn, final LivingEntity target)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.checkMemory(MemoryModules.ATTACKTARGET, MemoryStatus.REGISTERED))
            brain.setMemory(MemoryModules.ATTACKTARGET, target);
        if (mobIn instanceof Mob) ((Mob) mobIn).setTarget(target);
    }

    public static void setHuntTarget(final LivingEntity mobIn, final LivingEntity target)
    {
        Brain<?> brain = mobIn.getBrain();
        if (brain.checkMemory(MemoryModules.HUNTTARGET, MemoryStatus.REGISTERED))
            brain.setMemory(MemoryModules.HUNTTARGET, target);
        BrainUtils.setAttackTarget(mobIn, target);
        if (target != null)
        {
            brain = target.getBrain();
            if (brain.checkMemory(MemoryModules.HUNTED_BY, MemoryStatus.REGISTERED))
                brain.setMemory(MemoryModules.HUNTED_BY, mobIn);
        }
    }

    public static LivingEntity getHuntTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemoryValue(MemoryModules.HUNTTARGET)) return brain.getMemory(MemoryModules.HUNTTARGET).get();
        else if (mobIn instanceof Mob) return ((Mob) mobIn).getTarget();
        else return null;
    }

    public static boolean hasHuntTarget(final LivingEntity mobIn)
    {
        return BrainUtils.getHuntTarget(mobIn) != null;
    }

    public static AgeableMob getMateTarget(final AgeableMob mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemoryValue(MemoryModules.MATE_TARGET)) return brain.getMemory(MemoryModules.MATE_TARGET).get();
        else return null;
    }

    public static boolean hasMateTarget(final AgeableMob mobIn)
    {
        return BrainUtils.getMateTarget(mobIn) != null;
    }

    public static void setMateTarget(final AgeableMob mobIn, final AgeableMob target)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.checkMemory(MemoryModules.MATE_TARGET, MemoryStatus.REGISTERED))
            brain.setMemory(MemoryModules.MATE_TARGET, target);
    }

    public static void setMoveUseTarget(final LivingEntity mobIn, final Vector3 pos)
    {
        BrainUtils.setMoveUseTarget(mobIn, new VectorPosWrapper(pos));
    }

    public static void setMoveUseTarget(final LivingEntity mobIn, final PositionTracker pos)
    {
        final Brain<?> brain = mobIn.getBrain();
        brain.setMemory(MemoryModules.MOVE_TARGET, pos);
    }

    public static void clearMoveUseTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        brain.eraseMemory(MemoryModules.MOVE_TARGET);
    }

    public static boolean hasMoveUseTarget(final LivingEntity mobIn)
    {
        return BrainUtils.getMoveUseTarget(mobIn) != null;
    }

    public static PositionTracker getMoveUseTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (!brain.hasMemoryValue(MemoryModules.MOVE_TARGET)) return null;
        final Optional<PositionTracker> pos = brain.getMemory(MemoryModules.MOVE_TARGET);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static PositionTracker getLeapTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (!brain.hasMemoryValue(MemoryModules.LEAP_TARGET)) return null;
        final Optional<PositionTracker> pos = brain.getMemory(MemoryModules.LEAP_TARGET);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static void setLeapTarget(final LivingEntity mobIn, final PositionTracker target)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (!brain.checkMemory(MemoryModules.LEAP_TARGET, MemoryStatus.REGISTERED)) return;
        if (target == null) brain.eraseMemory(MemoryModules.LEAP_TARGET);
        else brain.setMemory(MemoryModules.LEAP_TARGET, target);
        final IPokemob mob = CapabilityPokemob.getPokemobFor(mobIn);
        if (mob != null) mob.setCombatState(CombatStates.LEAPING, target != null);
    }

    public static List<NearBlock> getNearBlocks(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        final Optional<List<NearBlock>> pos = brain.getMemory(MemoryModules.VISIBLE_BLOCKS);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static List<ItemEntity> getNearItems(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        final Optional<List<ItemEntity>> pos = brain.getMemory(MemoryModules.VISIBLE_ITEMS);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static List<AgeableMob> getMates(final AgeableMob entity)
    {
        final Brain<?> brain = entity.getBrain();
        final Optional<List<AgeableMob>> pos = brain.getMemory(MemoryModules.POSSIBLE_MATES);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static void initiateCombat(final Mob mob, LivingEntity target)
    {
        // No target self
        if (mob == target) return;
        // No target null
        if (target == null) return;
        // No target dead
        if (!target.isAlive() || target.getHealth() <= 0) return;

        final SetAttackTargetEvent event = new SetAttackTargetEvent(mob, target);
        MinecraftForge.EVENT_BUS.post(event);

        target = event.newTarget;
        // No target self
        if (mob == target) return;
        // No target null
        if (target == null) return;
        // No target already had target
        if (target == BrainUtils.getAttackTarget(mob)) return;

        final IPokemob aggressor = CapabilityPokemob.getPokemobFor(mob);
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);

        // No target an invalid target!
        if (aggressor != null && !AITools.validTargets.test(target)) return;
        if (targetMob != null && !AITools.validTargets.test(mob)) return;

        if (targetMob != null)
        {
            targetMob.setCombatState(CombatStates.ANGRY, true);
            targetMob.onSetTarget(mob, true);
        }
        if (aggressor != null)
        {
            aggressor.setCombatState(CombatStates.ANGRY, true);
            aggressor.onSetTarget(target, true);
        }

        BrainUtils.setAttackTarget(mob, target);
        BrainUtils.setAttackTarget(target, mob);
    }

    public static void deagro(final LivingEntity mob)
    {
        BrainUtils.deagro(mob, true);
    }

    public static void deagro(final LivingEntity mob, final boolean mutual)
    {
        if (mob == null) return;
        final IPokemob aggressor = CapabilityPokemob.getPokemobFor(mob);
        if (PokecubeMod.debug) if (mutual) PokecubeCore.LOGGER.error(mob + " " + mutual, new Exception());
        else PokecubeCore.LOGGER.error(mob + " " + mutual);
        if (aggressor != null)
        {
            aggressor.getTargetFinder().clear();
            aggressor.onSetTarget(null, true);
            aggressor.setCombatState(CombatStates.ANGRY, false);
            aggressor.setCombatState(CombatStates.MATEFIGHT, false);
        }
        final LivingEntity oldTarget = BrainUtils.getAttackTarget(mob);
        if (oldTarget != null && mutual) BrainUtils.deagro(oldTarget, false);
        BrainUtils.clearAttackTarget(mob);
        mob.getBrain().eraseMemory(MemoryModules.ATTACKTARGET);
        mob.getBrain().eraseMemory(MemoryModules.MATE_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
    }
}
