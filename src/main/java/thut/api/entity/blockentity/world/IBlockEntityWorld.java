package thut.api.entity.blockentity.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import thut.api.entity.blockentity.IBlockEntity;

public interface IBlockEntityWorld extends LevelAccessor
{
    default BlockState getBlock(final BlockPos pos)
    {
        if (!this.inBounds(pos)) return null;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - Mth.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - Mth.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - Mth.floor(entity.getZ() + mob.getMin().getZ());
        return mob.getBlocks()[i][j][k];
    }

    IBlockEntity getBlockEntity();

    default Level getWorld()
    {
        final Entity entity = (Entity) this.getBlockEntity();
        return entity.getCommandSenderWorld();
    }

    default BlockEntity getTile(BlockPos pos)
    {
        if (!this.inBounds(pos)) return null;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        pos = pos.immutable();
        final int i = pos.getX() - Mth.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - Mth.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - Mth.floor(entity.getZ() + mob.getMin().getZ());
        final BlockEntity tile = mob.getTiles()[i][j][k];
        if (tile != null && !tile.getBlockPos().equals(pos))
        {
            final CompoundTag tag = tile.serializeNBT();
            mob.getTiles()[i][j][k] = BlockEntity.loadStatic(pos, mob.getBlocks()[i][j][k], tag);
        }
        return tile;
    }

    default boolean inBounds(final BlockPos pos)
    {
        final IBlockEntity mob = this.getBlockEntity();
        if (mob.getBlocks() == null) return false;
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - Mth.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - Mth.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - Mth.floor(entity.getZ() + mob.getMin().getZ());
        if (i >= mob.getBlocks().length || j >= mob.getBlocks()[0].length || k >= mob.getBlocks()[0][0].length || i < 0
                || j < 0 || k < 0) return false;
        return true;
    }

    default boolean setBlock(final BlockPos pos, final BlockState state)
    {
        if (!this.inBounds(pos)) return false;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - Mth.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - Mth.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - Mth.floor(entity.getZ() + mob.getMin().getZ());
        mob.getBlocks()[i][j][k] = state;
        return true;
    }

    default void setBlockEntity(final IBlockEntity mob)
    {
        final Entity entity = (Entity) mob;
        final int xMin = mob.getMin().getX();
        final int zMin = mob.getMin().getZ();
        final int yMin = mob.getMin().getY();
        if (mob.getBlocks() == null)
        {
            if (!entity.getCommandSenderWorld().isClientSide) entity.discard();
            return;
        }
        final int sizeX = mob.getBlocks().length;
        final int sizeY = mob.getBlocks()[0].length;
        final int sizeZ = mob.getBlocks()[0][0].length;
        BlockEntity tile;
        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                    if ((tile = mob.getTiles()[i][j][k]) != null)
                    {
                        final BlockPos pos = new BlockPos(i + xMin + entity.getX(), j + yMin + entity.getY(), k + zMin
                                + entity.getZ());
                        final BlockPos old = tile.getBlockPos();
                        if (!old.equals(pos))
                        {
                            final CompoundTag tag = tile.serializeNBT();
                            mob.getTiles()[i][j][k] = BlockEntity.loadStatic(pos, mob.getBlocks()[i][j][k], tag);
                        }
                    }
    }

    default boolean setTile(final BlockPos pos, final BlockEntity tile)
    {
        if (!this.inBounds(pos)) return false;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - Mth.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - Mth.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - Mth.floor(entity.getZ() + mob.getMin().getZ());
        mob.getTiles()[i][j][k] = tile;
        if (tile != null && !tile.getBlockPos().equals(pos))
        {
            final CompoundTag tag = tile.serializeNBT();
            mob.getTiles()[i][j][k] = BlockEntity.loadStatic(pos, mob.getBlocks()[i][j][k], tag);
        }
        return true;
    }

    default HitResult trace(final ClipContext context)
    {
        return this.getWorld().clip(context);
    }
}
