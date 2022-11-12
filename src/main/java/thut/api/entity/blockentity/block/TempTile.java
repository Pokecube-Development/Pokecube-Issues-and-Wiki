package thut.api.entity.blockentity.block;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.block.ITickTile;
import thut.api.entity.blockentity.BlockEntityBase;
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
                // TODO Auto-generated catch block
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

    public VoxelShape getShape()
    {
        VoxelShape ret = Shapes.empty();
        if (this.blockEntity != null)
        {
            final Vector3 r = new Vector3().set(this.worldPosition);
            final VoxelShape shape = this.blockEntity.getUpdater().buildShape();
            if (!shape.isEmpty()) ret = Shapes.join(Shapes.block(), shape.move(-r.x, -r.y, -r.z), BooleanOp.AND);
        }
        this.shape = ret;
        return ret;
    }

    public float onVerticalCollide(Entity entity, float distance)
    {
        if (entity == this.blockEntity) return distance;
        if (this.blockEntity == null) return distance;
        var tileV = this.blockEntity.getV();
        var entityV = entity.getDeltaMovement();

        VoxelShape here = this.getShape();

        int n = 1;
        var below = level.getBlockEntity(getBlockPos().below(n));
        while (below instanceof TempTile tile)
        {
            var there = tile.getShape();
            if (here.isEmpty() && !there.isEmpty()) here = there.move(0, -n, 0);
            else if (!there.isEmpty())
            {
                here = Shapes.join(here, there.move(0, -n, 0), BooleanOp.OR);
            }
            n++;
            below = level.getBlockEntity(getBlockPos().below(n));
        }

        // We run this here, as this calls more often than below. This should
        // prevent players getting stuck in a kicked state.
        if (entity instanceof ServerPlayer serverplayer)
        {
            // Meed to set floatingTickCount to prevent being kicked
            serverplayer.connection.aboveGroundVehicleTickCount = 0;
            serverplayer.connection.aboveGroundTickCount = 0;
        }

        // Now make sure the mob is on top.
        if (!here.isEmpty())
        {
            double newVy = tileV.y() > 0 ? Math.max(tileV.y(), entityV.y()) : Math.min(tileV.y(), entityV.y());
            if (tileV.y() == 0) newVy = entityV.y();

            // Ensure the mob has same vertical velocity as us.
            entity.setDeltaMovement(entityV.x(), newVy, entityV.z());

            AABB bounds = here.bounds().move(this.getBlockPos()).move(this.blockEntity.getA());

            Player player = entity instanceof Player p ? p : null;
            // Due to differences in local vs remote player motion, we need to
            // only adjust by tileV if this is not the local player.
            if (player == null || !player.isLocalPlayer()) bounds = bounds.move(tileV);

            var entityR = entity.position();
            double x = entityR.x();
            double y = bounds.maxY + tileV.y();
            double z = entityR.z();

            if (tileV.y() > 0) entity.setPos(x, y, z);

            blockEntity.recentCollides.put(entity, entity.position().subtract(blockEntity.position()));

            double d0 = entity.getX();
            double d1 = entity.getY();
            double d2 = entity.getZ();
            entityV = entity.getDeltaMovement();

            entity.xOld = entity.xo = d0 - entityV.x;
            entity.yOld = entity.yo = d1 - entityV.y;
            entity.zOld = entity.zo = d2 - entityV.z;

            // Due to how minecraft handles players, this should be applied to
            // the client player instead, and let the server player get the info
            // from there.
            if (entity instanceof ServerPlayer serverplayer) serverplayer.fallDistance = 0;
        }
        return distance;
    }

}
