package pokecube.core.ai.routes;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.npc.Activities;

public class GuardTask<T extends LivingEntity> extends RootTask<T>
{
    final GuardAI goal;

    public GuardTask(final T mob, final GuardAI goal)
    {
        super(mob, ImmutableMap.of());
        this.goal = goal;
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn,
            final long gameTimeIn)
    {
        return this.goal.canContinueToUse();
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        final boolean valid = this.goal.canUse();
        if (!valid && owner.getBrain().isActive(Activities.STATIONARY)) owner.getBrain().setActiveActivityIfPossible(Activity.IDLE);
        return valid;
    }

    @Override
    protected void start(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().setActiveActivityIfPossible(Activities.STATIONARY);
        this.goal.start();
    }

    @Override
    protected void stop(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().setActiveActivityIfPossible(Activity.IDLE);
        this.goal.stop();
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        this.goal.tick();
    }

    @Override
    protected boolean timedOut(final long gameTime)
    {
        return !this.goal.canContinueToUse();
    }

}
