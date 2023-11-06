package thut.api.entity.blockentity.block;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Sets;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.block.ITickTile;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.BlockEntityBase.RelativeEntityPos;
import thut.api.maths.Vector3;
import thut.crafts.ThutCrafts;

public class TempTile extends BlockEntity implements ITickTile
{
    private static Set<BlockState> NO_INTERACT = Sets.newHashSet();

    public BlockEntityBase blockEntity;

    public VoxelShape shape = null;

    public TempTile(final BlockPos pos, final BlockState state)
    {
        super(ThutCrafts.CRAFTTE.get(), pos, state);
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

    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos, final Player player,
            final InteractionHand hand, final BlockHitResult hit)
    {
        final BlockState eff = this.getEffectiveState();
        if (eff != null && !NO_INTERACT.contains(eff) && blockEntity.getFakeWorld() instanceof Level level)
        {
            InteractionResult res = InteractionResult.PASS;
            try
            {
                BlockEntity be = this.getEffectiveTile();
                if (be != null && be.getLevel() == null) be.setLevel(level);
                res = eff.use(level, player, hand, hit);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                NO_INTERACT.add(eff);
            }
            if (res != InteractionResult.PASS) return res;
        }
        // Otherwise forward the interaction to the block entity;
        return blockEntity.interactAtFromTile(player, hit.getLocation(), hand);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        final BlockEntity effective = this.getEffectiveTile();
        if (effective != null && !(effective instanceof TempTile)) return effective.getCapability(cap, side);
        return super.getCapability(cap, side);
    }

    public VoxelShape getShape(boolean forCollide)
    {
        VoxelShape ret = Shapes.empty();
        if (this.blockEntity != null)
        {
            final Vector3 r = new Vector3().set(this.worldPosition);
            final VoxelShape shape = this.blockEntity.getUpdater().buildShape();
            if (!shape.isEmpty()) ret = Shapes.join(Shapes.block(), shape.move(-r.x, -r.y, -r.z), BooleanOp.AND);
            if (forCollide && blockEntity.getV().y() > 0 && !ret.isEmpty())
            {
                BlockEntity above = getLevel().getBlockEntity(getBlockPos().above());
                boolean empty = true;
                if (above instanceof TempTile tile)
                {
                    var s2 = tile.getShape(false);
                    if (!s2.isEmpty() && s2.bounds().getYsize() > 0.9) empty = false;
                }
                // Walls should still be walls, just floors affected.
                if (empty) return Shapes.empty();
            }
        }
        this.shape = ret;
        return ret;
    }

    public float onVerticalCollide(Entity entity, float distance)
    {
        if (entity == this.blockEntity) return distance;
        if (this.blockEntity == null) return distance;
        this.blockEntity.recentCollides.computeIfAbsent(entity, (e) -> {
            var v = new RelativeEntityPos(e, new AtomicInteger(), new Vector3f());
            v.lastSeen().set(this.blockEntity.tickCount + 20);
            float dx = (float) (entity.getX() - this.blockEntity.getX());
            float dy = (float) (entity.getY() - this.blockEntity.getY());
            float dz = (float) (entity.getZ() - this.blockEntity.getZ());
            if (v.relativePos().y() > 0) dy = v.relativePos().y();
            v.relativePos().set(dx, dy, dz);
            return v;
        });
        return distance;
    }

}
