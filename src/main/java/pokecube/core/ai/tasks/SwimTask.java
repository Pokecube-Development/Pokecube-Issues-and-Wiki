package pokecube.core.ai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.MobEntity;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.interfaces.IPokemob;

public class SwimTask extends RootTask<MobEntity>
{
    private final float maxDepth;
    private final float jumpChance;

    private final IPokemob pokemob;

    public SwimTask(final IPokemob pokemob, final float maxDepth, final float jumpChance)
    {
        super(ImmutableMap.of());
        this.maxDepth = maxDepth;
        this.jumpChance = jumpChance;
        this.pokemob = pokemob;
    }

    public SwimTask(final float maxDepth, final float jumpChance)
    {
        this(null, maxDepth, jumpChance);
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final MobEntity owner)
    {
        float d = this.maxDepth;
        final float h = owner.getHeight();
        if (h < 1) d *= h;
        if (this.pokemob != null && this.pokemob.swims()) return false;
        return owner.isInWater() && owner.getSubmergedHeight() > d || owner.isInLava();
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
