package thut.api.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IPosWrapper;
import net.minecraft.util.math.vector.Vector3d;
import thut.api.maths.Vector3;

public class VectorPosWrapper implements IPosWrapper
{
    final BlockPos bpos;
    final Vector3d vpos;
    final Vector3  pos;

    public VectorPosWrapper(final Vector3 pos)
    {
        this.bpos = pos.getPos().immutable();
        this.vpos = pos.toVec3d();
        this.pos = pos.copy();
    }

    @Override
    public BlockPos currentBlockPosition()
    {
        return this.bpos;
    }

    @Override
    public Vector3d currentPosition()
    {
        return this.vpos;
    }

    @Override
    public boolean isVisibleBy(final LivingEntity p_220610_1_)
    {
        return true;
    }

}
