package pokecube.core.ai.tasks.misc;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.MobEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.interfaces.IPokemob;

public class SwimTask extends RootTask<MobEntity>
{
    private final float jumpChance;

    private final IPokemob pokemob;

    public SwimTask(final IPokemob pokemob, final float jumpChance)
    {
        super(ImmutableMap.of());
        this.jumpChance = jumpChance;
        this.pokemob = pokemob;
    }

    public SwimTask(final float jumpChance)
    {
        this(null, jumpChance);
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final MobEntity owner)
    {
        if (this.pokemob != null && this.pokemob.swims()) return false;
        final boolean belowDepth = owner.func_233571_b_(FluidTags.WATER) > owner.func_233579_cu_();
        return owner.isInWater() && belowDepth || owner.isInLava();
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final MobEntity entityIn,
            final long gameTimeIn)
    {
        return this.shouldExecute(worldIn, entityIn);
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final MobEntity owner, final long gameTime)
    {
        if (owner.getRNG().nextFloat() < this.jumpChance) owner.getJumpController().setJumping();
    }
}
