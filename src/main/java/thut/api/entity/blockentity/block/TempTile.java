package thut.api.entity.blockentity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
        boolean shouldRemove = this.blockEntity == null;
        // Check if entity is actually alive
        if (!shouldRemove) shouldRemove = !this.blockEntity.isAlive();
        // Check if we are still in bounds.
        if (!shouldRemove) shouldRemove = !this.blockEntity.getBoundingBox().inflate(1.01)
                .contains(new Vec3(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ()));

        if (shouldRemove)
        {
            boolean water = this.getBlockState().getValue(TempBlock.WATERLOGGED);
            this.level.removeBlock(this.getBlockPos(), false);
            if (water) this.level.setBlock(getBlockPos(), Blocks.WATER.defaultBlockState(), 3);
        }
        else
        {
            final BlockState fake = this.getEffectiveState();
            final BlockState real = this.getBlockState();
            if (fake != null)
            {
                final int lightR = real.getLightEmission(this.getLevel(), this.getBlockPos());
                @SuppressWarnings("deprecation")
                final int lightF = fake.getLightEmission();
                if (lightR != lightF)
                    this.getLevel().setBlockAndUpdate(this.getBlockPos(), real.setValue(TempBlock.LIGHTLEVEL, lightF));
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
