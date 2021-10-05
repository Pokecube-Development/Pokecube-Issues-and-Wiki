package thut.api.entity.blockentity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.block.ITickTile;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.maths.Vector3;

public class TempTile extends BlockEntity implements ITickTile
{
    public static BlockEntityType<TempTile> TYPE;

    public BlockEntityBase blockEntity;

    public VoxelShape shape = null;

    public TempTile(final BlockPos pos, final BlockState state)
    {
        super(TempTile.TYPE, pos, state);
    }

    public TempTile(final BlockEntityBase blockEntity, final BlockPos pos, final BlockState state)
    {
        this(pos, state);
        this.blockEntity = blockEntity;
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
                final int lightR = real.getLightEmission(this.getLevel(), this.getBlockPos());
                @SuppressWarnings("deprecation")
                final int lightF = fake.getLightEmission();
                if (lightR != lightF) this.getLevel().setBlockAndUpdate(this.getBlockPos(), real.setValue(
                        TempBlock.LIGHTLEVEL, lightF));
            }
        }
    }

    public BlockEntity getEffectiveTile()
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
        final BlockEntity effective = this.getEffectiveTile();
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
        final AABB box = v.getAABB().expandTowards(1, 1, 1);
        final Vec3 ev = entityIn.getDeltaMovement();

        boolean serverSide = entityIn.getCommandSenderWorld().isClientSide;
        final boolean isPlayer = entityIn instanceof Player;
        if (isPlayer) serverSide = entityIn instanceof ServerPlayer;

        if (shapeHere.move(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()).bounds()
                .intersects(box))
        {
            final Vec3 bv = this.blockEntity.getDeltaMovement();
            final Vec3 dr = new Vec3(0, top - entityIn.getY(), 0);
            entityIn.setPos(entityIn.getX() + dr.x, entityIn.getY() + dr.y, entityIn.getZ() + dr.z);
            final double vx = ev.x;
            final double vy = bv.y;
            final double vz = ev.z;
            entityIn.setDeltaMovement(vx, vy, vz);

            if (isPlayer && serverSide)
            {
                final ServerPlayer serverplayer = (ServerPlayer) entityIn;
                // Meed to set floatingTickCount to prevent being kicked
                serverplayer.connection.aboveGroundVehicleTickCount = 0;
                serverplayer.connection.aboveGroundTickCount = 0;
            }
            return;
        }
    }

    public VoxelShape getShape()
    {
        VoxelShape ret = Shapes.empty();
        if (this.blockEntity != null)
        {
            final Vector3 r = Vector3.getNewVector().set(this.worldPosition);
            final VoxelShape shape = this.blockEntity.collider.buildShape();
            if (!shape.isEmpty()) ret = Shapes.join(Shapes.block(), shape.move(-r.x, -r.y, -r.z), BooleanOp.AND);
        }
        this.shape = ret;
        return ret;
    }

}
