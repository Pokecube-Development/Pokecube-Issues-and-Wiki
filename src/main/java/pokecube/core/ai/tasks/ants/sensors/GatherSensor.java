package pokecube.core.ai.tasks.ants.sensors;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.ants.AntTasks;

public class GatherSensor extends Sensor<Mob>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(AntTasks.WORK_POS,
            MemoryModules.VISIBLE_BLOCKS, AntTasks.NO_WORK_TIME);

    @Override
    protected void doTick(final ServerLevel worldIn, final Mob entityIn)
    {
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return GatherSensor.MEMS;
    }

}
