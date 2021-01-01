package thut.api.entity.blockentity.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.maths.Vector3;

public class TempTile extends TileEntity implements ITickableTileEntity
{
    public static TileEntityType<TempTile> TYPE;

    public BlockEntityBase blockEntity;

    public VoxelShape shape = null;

    public TempTile()
    {
        super(TempTile.TYPE);
    }

    public TempTile(final BlockEntityBase blockEntity)
    {
        super(TempTile.TYPE);
    }

    @Override
    public void tick()
    {
        if (this.blockEntity != null && !this.blockEntity.isAddedToWorld()) this.shape = null;
        if (this.blockEntity != null && !this.blockEntity.isAlive()) this.shape = null;
        if (this.shape == null || this.shape.isEmpty()) this.world.removeBlock(this.getPos(), false);
        else
        {
            final BlockState fake = this.getEffectiveState();
            final BlockState real = this.getBlockState();
            if (fake != null)
            {
                final int lightR = real.getLightValue(this.getWorld(), this.getPos());
                final int lightF = fake.getLightValue();
                if (lightR != lightF) this.getWorld().setBlockState(this.getPos(), real.with(TempBlock.LIGHTLEVEL,
                        lightF));
            }
        }
    }

    public TileEntity getEffectiveTile()
    {
        if (this.blockEntity != null) return this.blockEntity.getFakeWorld().getTile(this.getPos());
        return null;
    }

    public BlockState getEffectiveState()
    {
        if (this.blockEntity != null) return this.blockEntity.getFakeWorld().getBlock(this.getPos());
        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        final TileEntity effective = this.getEffectiveTile();
        if (effective != null) return effective.getCapability(cap, side);
        return super.getCapability(cap, side);
    }

    public void onEntityCollision(final Entity entityIn)
    {
        if (this.blockEntity == null) return;
        final VoxelShape shapeHere = this.getShape();
        double top = shapeHere.getEnd(Axis.Y);
        // Not a top surface.
        if (top > 1 || top < 0) return;
        top = top + this.pos.getY();
        final Vector3 v = Vector3.getNewVector().set(this.pos);
        final AxisAlignedBB box = v.getAABB().expand(1, 1, 1);
        final Vector3d ev = entityIn.getMotion();
        if (shapeHere.withOffset(this.pos.getX(), this.pos.getY(), this.pos.getZ()).getBoundingBox().intersects(box))
        {
            final Vector3d bv = this.blockEntity.getMotion();
            final Vector3d dr = new Vector3d(0, top - entityIn.getPosY(), 0);
            entityIn.setPosition(dr.x, dr.y, dr.z);
            final double vx = ev.x;
            final double vy = bv.y;
            final double vz = ev.z;
            entityIn.setMotion(vx, vy, vz);
            return;
        }
    }

    public VoxelShape getShape()
    {
        VoxelShape ret = VoxelShapes.empty();
        if (this.blockEntity != null)
        {
            final Vector3 r = Vector3.getNewVector().set(this.pos);
            final VoxelShape shape = this.blockEntity.collider.buildShape();
            if (!shape.isEmpty()) ret = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), shape.withOffset(-r.x,
                    -r.y, -r.z), IBooleanFunction.AND);
        }
        this.shape = ret;
        return ret;
    }

}
