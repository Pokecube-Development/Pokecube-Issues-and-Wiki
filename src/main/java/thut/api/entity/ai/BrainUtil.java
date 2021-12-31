package thut.api.entity.ai;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import pokecube.core.ai.brain.BrainUtils;
import thut.api.maths.Vector3;

/**
 * This class contains helper functions for maniplulating LivingEntity brains.
 */
public class BrainUtil
{
    // Utility functions for mob AI
    /**
     * Checks if the memory matches the validator.
     *
     * @param brain     - the brain of the mob
     * @param memory    - the memeory to check
     * @param validator - how to determine if it is valid
     * @return if the memory matches validator
     */
    public static boolean targetIsValid(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memory,
            Predicate<LivingEntity> validator)
    {
        return brain.getMemory(memory).filter(validator).filter(LivingEntity::isAlive).filter((mob) -> {
            return BehaviorUtils.entityIsVisible(brain, mob);
        }).isPresent();
    }

    /**
     * Checks if the mob can see the target.
     *
     * @param mobIn  - the mob to check memory for
     * @param target - the mob to see if is visible
     * @return if the mobIn can see target
     */
    public static boolean canSee(final LivingEntity mobIn, final LivingEntity target)
    {
        final boolean brainMemory = mobIn.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        boolean canSee = brainMemory && BehaviorUtils.entityIsVisible(mobIn.getBrain(), target);
        if (!brainMemory) canSee = mobIn.hasLineOfSight(target);
        return canSee;
    }

    /**
     * Makes the entity look at the giving location
     *
     * @param entityIn - the entity to do the looking
     * @param x        - the x coordinate to look at
     * @param y        - the y coordinate to look at
     * @param z        - the z coordinate to look at
     */
    public static void lookAt(final LivingEntity entityIn, final double x, final double y, final double z)
    {
        BrainUtils.lookAt(entityIn, Vector3.getNewVector().set(x, y, z));
    }

    /**
     * Makes the entity look at the giving location
     *
     * @param entityIn - the entity to do the looking
     * @param vec      - the location to look at
     */
    public static void lookAt(final LivingEntity entityIn, final Vector3 vec)
    {
        entityIn.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new VectorPosWrapper(vec));
    }

    // Functions for adding/removing things from brains

    /**
     * Removes the sensors.
     *
     * @param brain        the brain
     * @param SENSOR_TYPES the sensor types
     */
    public static void removeSensors(final Brain<?> brain, final List<SensorType<?>> SENSOR_TYPES)
    {
        for (final SensorType<?> type : SENSOR_TYPES) brain.sensors.remove(type);
    }

    /**
     * Adds the to brain.
     *
     * @param brain        the brain
     * @param MEMORY_TYPES the memory types
     * @param SENSOR_TYPES the sensor types
     */
    public static void addToBrain(final Brain<?> brain, final List<MemoryModuleType<?>> MEMORY_TYPES,
            final List<SensorType<?>> SENSOR_TYPES)
    {
        MEMORY_TYPES.forEach((module) -> {
            // Only add the memory module if it wasn't already added!
            if (!brain.memories.containsKey(module)) brain.memories.put(module, Optional.empty());
        });
        SENSOR_TYPES.forEach((type) -> {
            @SuppressWarnings("unchecked")
            final SensorType<? extends Sensor<? super LivingEntity>> stype = (SensorType<? extends Sensor<? super LivingEntity>>) type;
            @SuppressWarnings("unchecked")
            final Sensor<LivingEntity> sense = (Sensor<LivingEntity>) stype.create();
            brain.sensors.put(stype, sense);
        });
        brain.sensors.values().forEach((sensor) -> {
            for (final MemoryModuleType<?> memorymoduletype : sensor.requires())
                if (!brain.memories.containsKey(memorymoduletype))
                    brain.memories.put(memorymoduletype, Optional.empty());
        });

    }

    /**
     * Removes the matching tasks.
     *
     * @param brain the brain
     * @param match the predicate to match
     */
    public static void removeMatchingTasks(final Brain<?> brain, final Predicate<Behavior<?>> match)
    {
        brain.availableBehaviorsByPriority.forEach((i, map) -> map.values().forEach(s -> s.removeIf(match)));
    }

    /**
     * Adds to activity.
     *
     * @param brain the brain
     * @param act   the Activity to add
     * @param tasks the tasks
     */
    public static void addToActivity(final Brain<?> brain, final Activity act,
            final Collection<Pair<Integer, ? extends Behavior<? super LivingEntity>>> tasks)
    {
        tasks.forEach((pair) -> {
            final Integer prior = pair.getFirst();
            final Behavior<? super LivingEntity> task = pair.getSecond();
            brain.availableBehaviorsByPriority.computeIfAbsent(prior, (val) -> {
                return Maps.newHashMap();
            }).computeIfAbsent(act, (tmp) -> {
                return Sets.newLinkedHashSet();
            }).add(task);
        });
    }
}
