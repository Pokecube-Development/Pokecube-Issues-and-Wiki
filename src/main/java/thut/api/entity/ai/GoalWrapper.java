package thut.api.entity.ai;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal;

public class GoalWrapper extends Goal
{
    final IAIRunnable wrapped;

    public GoalWrapper(IAIRunnable wrap)
    {
        this.wrapped = wrap;
    }

    public GoalWrapper(IAIRunnable wrap, EnumSet<Flag> flags)
    {
        this.wrapped = wrap;
        this.setMutexFlags(flags);
    }

    @Override
    public EnumSet<Flag> getMutexFlags()
    {
        return super.getMutexFlags();
    }

    @Override
    public boolean isPreemptible()
    {
        return super.isPreemptible();
    }

    @Override
    public void resetTask()
    {
        this.wrapped.reset();
    }

    @Override
    public void setMutexFlags(EnumSet<Flag> p_220684_1_)
    {
        // TODO Auto-generated method stub
        super.setMutexFlags(p_220684_1_);
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
    }

}
