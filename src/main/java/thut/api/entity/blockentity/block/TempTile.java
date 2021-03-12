package thut.api.entity.blockentity.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
        if (this.shape == null || this.shape.isEmpty()) this.level.removeBlock(this.getBlockPos(), false);
        else
        {
            final BlockState fake = this.getEffectiveState();
            final BlockState real = this.getBlockState();
            if (fake != null)
            {
                final int lightR = real.getLightValue(this.getLevel(), this.getBlockPos());
                @SuppressWarnings("deprecation")
                final int lightF = fake.getLightEmission();
                if (lightR != lightF) this.getLevel().setBlockAndUpdate(this.getBlockPos(), real.setValue(TempBlock.LIGHTLEVEL,
                        lightF));
            }
        }
    }

    public TileEntity getEffectiveTile()
    {
        if (this.blockEntity != null) return this.blockEntity.getFakeWorld().getTile(this.getBlockPos());
        return null;
    }

    public BlockState getEffectiveState()
    {
        if (this.blockEntity != null) return this.blockEntity.getFakeWorld().getBlock(this.getBlockPos());
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
        double top = shapeHere.max(Axis.Y);
        // Not a top surface.
        if (top > 1 || top < 0) return;
        top = top + this.worldPosition.getY();
        final Vector3 v = Vector3.getNewVector().set(this.worldPosition);
        final AxisAlignedBB box = v.getAABB().expandTowards(1, 1, 1);
        final Vector3d ev = entityIn.getDeltaMovement();

        boolean serverSide = entityIn.getCommandSenderWorld().isClientSide;
        final boolean isPlayer = entityIn instanceof PlayerEntity;
        if (isPlayer) serverSide = entityIn instanceof ServerPlayerEntity;

        if (shapeHere.move(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()).bounds().intersects(box))
        {
            final Vector3d bv = this.blockEntity.getDeltaMovement();
            final Vector3d dr = new Vector3d(0, top - entityIn.getY(), 0);
            entityIn.setPos(entityIn.getX() + dr.x, entityIn.getY() + dr.y, entityIn.getZ() + dr.z);
            final double vx = ev.x;
            final double vy = bv.y;
            final double vz = ev.z;
            entityIn.setDeltaMovement(vx, vy, vz);

            if (isPlayer && serverSide)
            {
                final ServerPlayerEntity serverplayer = (ServerPlayerEntity) entityIn;
                // Meed to set floatingTickCount to prevent being kicked
                serverplayer.connection.aboveGroundVehicleTickCount = 0;
                serverplayer.connection.aboveGroundTickCount = 0;
            }
            return;
        }
    }

    public VoxelShape getShape()
    {
        VoxelShape ret = VoxelShapes.empty();
        if (this.blockEntity != null)
        {
            final Vector3 r = Vector3.getNewVector().set(this.worldPosition);
            final VoxelShape shape = this.blockEntity.collider.buildShape();
            if (!shape.isEmpty()) ret = VoxelShapes.join(VoxelShapes.block(), shape.move(-r.x,
                    -r.y, -r.z), IBooleanFunction.AND);
        }
        this.shape = ret;
        return ret;
    }

}
