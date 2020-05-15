package thut.api.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

public class TaskWrapper<E extends LivingEntity> extends Task<E>
{
    final ITask wrapped;

    public TaskWrapper(final ITask wrap)
    {
        super(wrap.getNeededMemories());
        this.wrapped = wrap;
    }

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final E owner)
    {
        return this.wrapped.shouldRun();
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        this.wrapped.reset();
    }

    @Override
    protected boolean isTimedOut(final long gameTime)
    {
        return super.isTimedOut(gameTime);
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        return this.wrapped.shouldRun();
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final E owner, final long gameTime)
    {
        this.wrapped.run();
        this.wrapped.tick();
        this.wrapped.finish();
    }

}
