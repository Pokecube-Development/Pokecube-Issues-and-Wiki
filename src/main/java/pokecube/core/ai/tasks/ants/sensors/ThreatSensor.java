package pokecube.core.ai.tasks.ants.sensors;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.tasks.ants.AntTasks;

public class ThreatSensor extends Sensor<MobEntity>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(AntTasks.NEST_POS,
            MemoryModuleType.VISIBLE_MOBS, AntTasks.WORK_POS);

    @Override
    protected void update(final ServerWorld worldIn, final MobEntity entityIn)
    {

    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return ThreatSensor.MEMS;
    }

}
