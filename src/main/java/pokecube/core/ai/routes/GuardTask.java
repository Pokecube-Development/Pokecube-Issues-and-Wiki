package pokecube.core.ai.routes;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.npc.Activities;

public class GuardTask<T extends LivingEntity> extends Task<LivingEntity>
{
    final GuardAI goal;

    public GuardTask(final GuardAI goal)
    {
        super(ImmutableMap.of());
        this.goal = goal;
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final LivingEntity entityIn,
            final long gameTimeIn)
    {
        return this.goal.shouldContinueExecuting();
    }

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final LivingEntity owner)
    {
        final boolean valid = this.goal.shouldExecute();
        if (!valid && owner.getBrain().hasActivity(Activities.STATIONARY)) owner.getBrain().switchTo(Activity.IDLE);
        return valid;
    }

    @Override
    protected void startExecuting(final ServerWorld worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().switchTo(Activities.STATIONARY);
        this.goal.startExecuting();
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().switchTo(Activity.IDLE);
        this.goal.resetTask();
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final LivingEntity owner, final long gameTime)
    {
        this.goal.tick();
    }

    @Override
    protected boolean isTimedOut(final long gameTime)
    {
        return !this.goal.shouldContinueExecuting();
    }

}
