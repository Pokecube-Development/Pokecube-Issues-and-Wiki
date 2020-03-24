package thut.api.entity.ai;

import java.util.List;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import thut.core.common.ThutCore;

public class GoalsWrapper extends Goal
{
    public final List<IAIRunnable> wrapped;
    private final Entity           mob;
    private int                    lastTick = -1;

    public GoalsWrapper(final Entity mob, final IAIRunnable... wrap)
    {
        this.wrapped = Lists.newArrayList(wrap);
        this.wrapped.sort((a, b) -> a.getPriority() - b.getPriority());
        this.mob = mob;
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
    public void resetTask()
    {
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
        if (this.lastTick == this.mob.ticksExisted) return;
        this.lastTick = this.mob.ticksExisted;
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
