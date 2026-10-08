package pokecube.gimmicks.nests.tasks.ants.sensors;

import java.util.Set;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import pokecube.core.ai.brain.MemoryModules;

public class GatherSensor extends Sensor<Mob>
{
    @Override
    protected void doTick(final ServerLevel worldIn, final Mob entityIn)
    {}

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(MemoryModules.WORK_POS.get(), MemoryModules.VISIBLE_BLOCKS.get(),
                MemoryModules.NO_WORK_TIMER.get());
    }

}
