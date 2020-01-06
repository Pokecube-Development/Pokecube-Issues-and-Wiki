package thut.api.entity.ai;

import java.util.EnumSet;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;

import net.minecraft.entity.ai.goal.Goal;
import thut.core.common.ThutCore;

public class GoalsWrapper extends Goal
{
    public final List<IAIRunnable> wrapped;

    public GoalsWrapper(final IAIRunnable... wrap)
    {
        this.wrapped = Lists.newArrayList(wrap);
        this.wrapped.sort((a, b) -> a.getPriority() - b.getPriority());
    }

    /**
     * Checks if task can run, given the tasks in tasks.
     *
     * @param task
     * @param tasks
     * @return
     */
    private boolean canRun(final IAIRunnable task)
    {
        final int prior = task.getPriority();
        final int mutex = task.getMutex();
        for (int i = 0; i < this.wrapped.size(); i++)
        {
            final IAIRunnable ai = this.wrapped.get(i);
            if (ai.getPriority() < prior && (mutex & ai.getMutex()) != 0 && ai.shouldRun()) return false;
        }
        return task.shouldRun();
    }

    @Override
    public EnumSet<Flag> getMutexFlags()
    {
        return super.getMutexFlags();
    }

    @Override
    public boolean isPreemptible()
    {
        return false;
    }

    @Override
    public void resetTask()
    {
    }

    @Override
    public void setMutexFlags(final EnumSet<Flag> p_220684_1_)
    {
        super.setMutexFlags(p_220684_1_);
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        return true;
    }

    @Override
    public boolean shouldExecute()
    {
        return true;
    }

    @Override
    public void startExecuting()
    {
    }

    @Override
    public void tick()
    {
        for (final IAIRunnable ai : this.wrapped)
            try
            {
                if (this.canRun(ai))
                {
                    ai.run();
                    ai.tick();
                    ai.finish();
                }
                else ai.reset();
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.log(Level.FATAL, "error checking task " + ai, e);
            }
    }

}
