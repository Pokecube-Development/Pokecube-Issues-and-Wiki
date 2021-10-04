package pokecube.core.ai.tasks.misc;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.RootTask;

public class LookAtTask extends RootTask<Mob>
{

    public LookAtTask(final int duration, final int maxDuration)
    {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), duration, maxDuration);
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final Mob entityIn,
            final long gameTimeIn)
    {
        return entityIn.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter((target) ->
        {
            return target.isVisibleBy(entityIn);
        }).isPresent();
    }

    @Override
    protected void stop(final ServerLevel worldIn, final Mob entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(final ServerLevel worldIn, final Mob owner, final long gameTime)
    {
        owner.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((pos) ->
        {
            owner.getLookControl().setLookAt(pos.currentPosition());
        });
    }
}
