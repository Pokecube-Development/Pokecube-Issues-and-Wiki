package pokecube.core.ai.tasks.idle;

import net.minecraft.entity.LivingEntity;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;

public class ForgetHuntedByTask extends TaskBase<LivingEntity>
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
        this.entity.getBrain().removeMemory(MemoryModules.HUNTED_BY);
    }

    @Override
    public void run()
    {
        this.fleeingTicks++;
    }

    @Override
    public boolean shouldRun()
    {
        return this.entity.getBrain().hasMemory(MemoryModules.HUNTED_BY) && this.fleeingTicks < this.duration;
    }

}
