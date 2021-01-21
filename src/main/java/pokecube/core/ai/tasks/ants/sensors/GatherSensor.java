package pokecube.core.ai.tasks.ants.sensors;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.ants.AntTasks;

public class GatherSensor extends Sensor<MobEntity>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(AntTasks.WORK_POS,
            MemoryModules.VISIBLE_BLOCKS, AntTasks.NO_WORK_TIME);

    @Override
    protected void update(final ServerWorld worldIn, final MobEntity entityIn)
    {
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return GatherSensor.MEMS;
    }

}
