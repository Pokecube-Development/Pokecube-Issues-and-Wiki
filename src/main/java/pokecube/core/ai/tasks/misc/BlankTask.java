package pokecube.core.ai.tasks.misc;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.RootTask;

public class BlankTask extends RootTask<LivingEntity>
{
    public BlankTask(final int durMin, final int durMax)
    {
        super(ImmutableMap.of(), durMin, durMax);
    }

    @Override
    protected boolean canStillUse(final ServerWorld worldIn, final LivingEntity entityIn,
            final long gameTimeIn)
    {
        return true;
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }
}
