package pokecube.core.ai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.RootTask;

public class LookAtTask extends RootTask<MobEntity>
{

    public LookAtTask(final int duration, final int maxDuration)
    {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_PRESENT), duration, maxDuration);
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final MobEntity entityIn,
            final long gameTimeIn)
    {
        return entityIn.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter((target) ->
        {
            return target.isVisibleTo(entityIn);
        }).isPresent();
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().removeMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final MobEntity owner, final long gameTime)
    {
        owner.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((pos) ->
        {
            owner.getLookController().setLookPosition(pos.getPos());
        });
    }
}
