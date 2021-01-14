package pokecube.core.ai.brain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.IPosWrapper;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.events.SetAttackTargetEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.utils.AITools;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.maths.Vector3;

public class BrainUtils
{
    public static boolean canSee(final LivingEntity mobIn, final LivingEntity target)
    {
        final boolean brainMemory = mobIn.getBrain().hasMemory(MemoryModuleType.VISIBLE_MOBS);
        boolean canSee = brainMemory && BrainUtil.canSee(mobIn.getBrain(), target);
        if (!brainMemory) canSee = mobIn.canEntityBeSeen(target);
        return canSee;
    }

    public static LivingEntity getAttackTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.ATTACKTARGET)) return brain.getMemory(MemoryModules.ATTACKTARGET).get();
        else if (mobIn instanceof MobEntity) return ((MobEntity) mobIn).getAttackTarget();
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
        if (brain.hasMemory(MemoryModules.ATTACKTARGET, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                MemoryModules.ATTACKTARGET, target);
        if (mobIn instanceof MobEntity) ((MobEntity) mobIn).setAttackTarget(target);
    }

    public static void setHuntTarget(final LivingEntity mobIn, final LivingEntity target)
    {
        Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.HUNTTARGET, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                MemoryModules.HUNTTARGET, target);
        BrainUtils.setAttackTarget(mobIn, target);
        if (target != null)
        {
            brain = target.getBrain();
            if (brain.hasMemory(MemoryModules.HUNTED_BY, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                    MemoryModules.HUNTED_BY, mobIn);
        }
    }

    public static LivingEntity getHuntTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.HUNTTARGET)) return brain.getMemory(MemoryModules.HUNTTARGET).get();
        else if (mobIn instanceof MobEntity) return ((MobEntity) mobIn).getAttackTarget();
        else return null;
    }

    public static boolean hasHuntTarget(final LivingEntity mobIn)
    {
        return BrainUtils.getHuntTarget(mobIn) != null;
    }

    public static AgeableEntity getMateTarget(final AgeableEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.MATE_TARGET)) return brain.getMemory(MemoryModules.MATE_TARGET).get();
        else return null;
    }

    public static boolean hasMateTarget(final AgeableEntity mobIn)
    {
        return BrainUtils.getMateTarget(mobIn) != null;
    }

    public static void setMateTarget(final AgeableEntity mobIn, final AgeableEntity target)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.MATE_TARGET, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                MemoryModules.MATE_TARGET, target);
    }

    public static void lookAt(final LivingEntity entityIn, final double x, final double y, final double z)
    {
        BrainUtils.lookAt(entityIn, Vector3.getNewVector().set(x, y, z));
    }

    public static void lookAt(final LivingEntity entityIn, final Vector3 vec)
    {
        entityIn.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new VectorPosWrapper(vec));
    }

    public static void setMoveUseTarget(final LivingEntity mobIn, final Vector3 pos)
    {
        BrainUtils.setMoveUseTarget(mobIn, new VectorPosWrapper(pos));
    }

    public static void setMoveUseTarget(final LivingEntity mobIn, final IPosWrapper pos)
    {
        final Brain<?> brain = mobIn.getBrain();
        brain.setMemory(MemoryModules.MOVE_TARGET, pos);
    }

    public static void clearMoveUseTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        brain.removeMemory(MemoryModules.MOVE_TARGET);
    }

    public static boolean hasMoveUseTarget(final LivingEntity mobIn)
    {
        return BrainUtils.getMoveUseTarget(mobIn) != null;
    }

    public static IPosWrapper getMoveUseTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (!brain.hasMemory(MemoryModules.MOVE_TARGET)) return null;
        final Optional<IPosWrapper> pos = brain.getMemory(MemoryModules.MOVE_TARGET);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static IPosWrapper getLeapTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (!brain.hasMemory(MemoryModules.LEAP_TARGET)) return null;
        final Optional<IPosWrapper> pos = brain.getMemory(MemoryModules.LEAP_TARGET);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static void setLeapTarget(final LivingEntity mobIn, final IPosWrapper target)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (!brain.hasMemory(MemoryModules.LEAP_TARGET, MemoryModuleStatus.REGISTERED)) return;
        if (target == null) brain.removeMemory(MemoryModules.LEAP_TARGET);
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

    public static List<AgeableEntity> getMates(final AgeableEntity entity)
    {
        final Brain<?> brain = entity.getBrain();
        final Optional<List<AgeableEntity>> pos = brain.getMemory(MemoryModules.POSSIBLE_MATES);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static void removeSensors(final Brain<?> brain, final List<SensorType<?>> SENSOR_TYPES)
    {
        for (final SensorType<?> type : SENSOR_TYPES)
            brain.sensors.remove(type);
    }

    public static void addToBrain(final Brain<?> brain, final List<MemoryModuleType<?>> MEMORY_TYPES,
            final List<SensorType<?>> SENSOR_TYPES)
    {
        MEMORY_TYPES.forEach((module) ->
        {
            // Only add the memory module if it wasn't already added!
            if (!brain.memories.containsKey(module)) brain.memories.put(module, Optional.empty());
        });
        SENSOR_TYPES.forEach((type) ->
        {
            @SuppressWarnings("unchecked")
            final SensorType<? extends Sensor<? super LivingEntity>> stype = (SensorType<? extends Sensor<? super LivingEntity>>) type;
            @SuppressWarnings("unchecked")
            final Sensor<LivingEntity> sense = (Sensor<LivingEntity>) stype.getSensor();
            brain.sensors.put(stype, sense);
        });
        brain.sensors.values().forEach((sensor) ->
        {
            for (final MemoryModuleType<?> memorymoduletype : sensor.getUsedMemories())
                if (!brain.memories.containsKey(memorymoduletype)) brain.memories.put(memorymoduletype, Optional
                        .empty());
        });

    }

    public static void addToActivity(final Brain<?> brain, final Activity act,
            final Collection<Pair<Integer, ? extends Task<? super LivingEntity>>> tasks)
    {
        tasks.forEach((pair) ->
        {
            final Integer prior = pair.getFirst();
            final Task<? super LivingEntity> task = pair.getSecond();
            brain.taskPriorityMap.computeIfAbsent(prior, (val) ->
            {
                return Maps.newHashMap();
            }).computeIfAbsent(act, (tmp) ->
            {
                return Sets.newLinkedHashSet();
            }).add(task);
        });
    }

    public static void initiateCombat(final MobEntity mob, LivingEntity target)
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
        mob.getBrain().removeMemory(MemoryModules.ATTACKTARGET);
        mob.getBrain().removeMemory(MemoryModules.MATE_TARGET);
        mob.getBrain().removeMemory(MemoryModuleType.HURT_BY_ENTITY);
        mob.getBrain().removeMemory(MemoryModuleType.HURT_BY);
    }
}
