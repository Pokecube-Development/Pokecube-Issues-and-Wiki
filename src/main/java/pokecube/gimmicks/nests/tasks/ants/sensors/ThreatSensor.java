package pokecube.gimmicks.nests.tasks.ants.sensors;

import java.util.Set;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import pokecube.core.ai.brain.MemoryModules;

public class ThreatSensor extends Sensor<Mob>
{

    @Override
    protected void doTick(final ServerLevel worldIn, final Mob entityIn)
    {

    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(MemoryModules.NEST_POS.get(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModules.WORK_POS.get());
    }

}
