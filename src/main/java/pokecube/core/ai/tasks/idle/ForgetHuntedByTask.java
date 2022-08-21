package pokecube.core.ai.tasks.idle;

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
    public void reset()
    {
        this.fleeingTicks = 0;
        this.entity.getBrain().eraseMemory(MemoryModules.HUNTED_BY.get());
    }

    @Override
    public void run()
    {
        this.fleeingTicks++;
    }

    @Override
    public boolean shouldRun()
    {
        return this.entity.getBrain().hasMemoryValue(MemoryModules.HUNTED_BY.get()) && this.fleeingTicks < this.duration;
    }

}
