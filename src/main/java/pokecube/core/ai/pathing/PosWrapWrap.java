package pokecube.core.ai.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

public class PosWrapWrap implements PositionTracker
{
    public final boolean canThrottle;

    private final PositionTracker wrapped;

    public PosWrapWrap(final PositionTracker toWrap, final boolean canThrottle)
    {
        this.canThrottle = canThrottle;
        this.wrapped = toWrap;
    }

    @Override
    public Vec3 currentPosition()
    {
        return this.wrapped.currentPosition();
    }

    @Override
    public BlockPos currentBlockPosition()
    {
        return this.wrapped.currentBlockPosition();
    }

    @Override
    public boolean isVisibleBy(final LivingEntity entity)
    {
        return this.wrapped.isVisibleBy(entity);
    }

}
