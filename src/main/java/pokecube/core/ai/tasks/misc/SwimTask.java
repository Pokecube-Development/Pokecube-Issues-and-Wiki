package pokecube.core.ai.tasks.misc;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.RootTask;

public class SwimTask extends RootTask<Mob>
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
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final Mob owner)
    {
        if (this.pokemob != null && this.pokemob.swims()) return false;
        final boolean belowDepth = owner.getFluidHeight(FluidTags.WATER) > owner.getFluidJumpThreshold();
        return owner.isInWater() && belowDepth || owner.isInLava();
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final Mob entityIn,
            final long gameTimeIn)
    {
        return this.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void tick(final ServerLevel worldIn, final Mob owner, final long gameTime)
    {
        if (owner.getRandom().nextFloat() < this.jumpChance) owner.getJumpControl().jump();
    }
}
