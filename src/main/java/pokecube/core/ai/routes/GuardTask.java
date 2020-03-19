package pokecube.core.ai.routes;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.npc.Activities;

public class GuardTask<T extends MobEntity> extends Task<MobEntity>
{
    final GuardAI  goal;
    final Brain<T> brain;

    public GuardTask(final Brain<T> brain, final GuardAI goal)
    {
        super(ImmutableMap.of());
        this.goal = goal;
        this.brain = brain;
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
        this.brain.switchTo(Activities.STATIONARY);
        this.goal.startExecuting();
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        this.brain.switchTo(Activity.IDLE);
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
