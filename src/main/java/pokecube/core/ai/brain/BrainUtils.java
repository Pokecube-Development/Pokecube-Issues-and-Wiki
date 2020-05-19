package pokecube.core.ai.brain;

import java.util.List;
import java.util.Optional;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;

public class BrainUtils
{
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

    public static void setAttackTarget(final LivingEntity mobIn, final LivingEntity target)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.ATTACKTARGET, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                MemoryModules.ATTACKTARGET, target);
        if (mobIn instanceof MobEntity) ((MobEntity) mobIn).setAttackTarget(target);
    }

    public static LivingEntity getHuntTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.ATTACKTARGET)) return brain.getMemory(MemoryModules.ATTACKTARGET).get();
        else if (mobIn instanceof MobEntity) return ((MobEntity) mobIn).getAttackTarget();
        else return null;
    }

    public static void addToBrain(final Brain<?> brain, final List<MemoryModuleType<?>> MEMORY_TYPES,
            final List<SensorType<?>> SENSOR_TYPES)
    {
        MEMORY_TYPES.forEach((module) ->
        {
            brain.memories.put(module, Optional.empty());
        });
        SENSOR_TYPES.forEach((type) ->
        {
            @SuppressWarnings("unchecked")
            final SensorType<? extends Sensor<? super LivingEntity>> stype = (SensorType<? extends Sensor<? super LivingEntity>>) type;
            @SuppressWarnings("unchecked")
            final Sensor<LivingEntity> sense = (Sensor<LivingEntity>) stype.func_220995_a();
            brain.sensors.put(stype, sense);
        });
        brain.sensors.values().forEach((sensor) ->
        {
            for (final MemoryModuleType<?> memorymoduletype : sensor.getUsedMemories())
                brain.memories.put(memorymoduletype, Optional.empty());
        });
    }
}
