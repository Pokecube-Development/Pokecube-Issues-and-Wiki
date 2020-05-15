package thut.api.entity.ai;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal;

public class GoalWrapper extends Goal
{
    final IAIRunnable wrapped;

    public GoalWrapper(final IAIRunnable wrap)
    {
        this.wrapped = wrap;
    }

    public GoalWrapper(final IAIRunnable wrap, final EnumSet<Flag> flags)
    {
        this.wrapped = wrap;
        this.setMutexFlags(flags);
    }

    @Override
    public void resetTask()
    {
        this.wrapped.reset();
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        return this.wrapped.shouldRun();
    }

    @Override
    public boolean shouldExecute()
    {
        return this.wrapped.shouldRun();
    }

    @Override
    public void startExecuting()
    {
        this.wrapped.firstRun();
    }

    @Override
    public void tick()
    {
        this.wrapped.run();
        this.wrapped.tick();
        this.wrapped.finish();
    }

}
