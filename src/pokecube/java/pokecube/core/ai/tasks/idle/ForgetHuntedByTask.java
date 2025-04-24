package pokecube.core.ai.tasks.idle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;

public class ForgetHuntedByTask extends TaskBase
{
    int fleeingTicks = 0;

    final int duration;

    public ForgetHuntedByTask(final IPokemob pokemob, final int duration)
    {
        super(pokemob);
        this.duration = duration;
    }

    @Override
    public void reset(Mob entityIn)
    {
        this.fleeingTicks = 0;
        this.entity.getBrain().eraseMemory(MemoryModules.HUNTED_BY.get());
    }

    @Override
    public void run(ServerLevel level, Mob owner)
    {
        this.fleeingTicks++;
    }

    @Override
    public boolean shouldRun(Mob entityIn)
    {
        return this.entity.getBrain().hasMemoryValue(MemoryModules.HUNTED_BY.get()) && this.fleeingTicks < this.duration;
    }

}
