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
    protected boolean checkExtraStartConditions(final ServerWorld worldIn, final MobEntity owner)
    {
        if (this.pokemob != null && this.pokemob.swims()) return false;
        final boolean belowDepth = owner.getFluidHeight(FluidTags.WATER) > owner.getFluidJumpThreshold();
        return owner.isInWater() && belowDepth || owner.isInLava();
    }

    @Override
    protected boolean canStillUse(final ServerWorld worldIn, final MobEntity entityIn,
            final long gameTimeIn)
    {
        return this.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void tick(final ServerWorld worldIn, final MobEntity owner, final long gameTime)
    {
        if (owner.getRandom().nextFloat() < this.jumpChance) owner.getJumpControl().jump();
    }
}
