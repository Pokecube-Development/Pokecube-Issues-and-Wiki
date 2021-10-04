package thut.api.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;
import thut.api.maths.Vector3;

public class VectorPosWrapper implements PositionTracker
{
    final BlockPos bpos;
    final Vec3 vpos;
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
    public Vec3 currentPosition()
    {
        return this.vpos;
    }

    @Override
    public boolean isVisibleBy(final LivingEntity p_220610_1_)
    {
        return true;
    }

}
