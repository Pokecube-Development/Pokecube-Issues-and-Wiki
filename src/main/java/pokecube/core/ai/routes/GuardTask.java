package pokecube.core.ai.routes;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

public class GuardTask extends Task<MobEntity>
{
    final GuardAI goal;

    public GuardTask(final GuardAI goal)
    {
        super(ImmutableMap.of());
        this.goal = goal;
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final MobEntity entityIn,
            final long gameTimeIn)
    {
        return this.goal.shouldContinueExecuting();
    }

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final MobEntity owner)
    {
        return this.goal.shouldExecute();
    }

    @Override
    protected void startExecuting(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        this.goal.startExecuting();
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        this.goal.resetTask();
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final MobEntity owner, final long gameTime)
    {
        this.goal.tick();
    }

    @Override
    protected boolean isTimedOut(final long gameTime)
    {
        return !this.goal.shouldContinueExecuting();
    }

}
