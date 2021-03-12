package pokecube.core.ai.pathing;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IPosWrapper;
import net.minecraft.util.math.vector.Vector3d;

public class PosWrapWrap implements IPosWrapper
{
    public final boolean canThrottle;

    private final IPosWrapper wrapped;

    public PosWrapWrap(final IPosWrapper toWrap, final boolean canThrottle)
    {
        this.canThrottle = canThrottle;
        this.wrapped = toWrap;
    }

    @Override
    public Vector3d currentPosition()
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
