package pokecube.core.ai.routes;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

public class GuardTask<T extends MobEntity> extends Task<MobEntity>
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
        entityIn.getBrain().switchTo(Activity.IDLE);
        this.goal.startExecuting();
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().switchTo(Activity.IDLE);
        this.goal.resetTask();
        entityIn.getNavigator().clearPath();
        entityIn.getBrain().removeMemory(MemoryModuleType.PATH);
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final MobEntity owner, final long gameTime)
    {
        this.goal.tick();
        if (owner.getNavigator().getPath() != null) owner.getBrain().setMemory(MemoryModuleType.PATH, owner
                .getNavigator().getPath());
    }

    @Override
    protected boolean isTimedOut(final long gameTime)
    {
        return !this.goal.shouldContinueExecuting();
    }

}
