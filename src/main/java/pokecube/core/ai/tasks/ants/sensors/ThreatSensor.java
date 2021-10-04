package pokecube.core.ai.tasks.ants.sensors;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import pokecube.core.ai.tasks.ants.AntTasks;

public class ThreatSensor extends Sensor<Mob>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(AntTasks.NEST_POS,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, AntTasks.WORK_POS);

    @Override
    protected void doTick(final ServerLevel worldIn, final Mob entityIn)
    {

    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return ThreatSensor.MEMS;
    }

}
