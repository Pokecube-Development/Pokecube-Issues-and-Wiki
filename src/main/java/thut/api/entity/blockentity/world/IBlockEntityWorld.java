package thut.api.entity.blockentity.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import thut.api.entity.blockentity.IBlockEntity;

public interface IBlockEntityWorld extends IWorld
{
    default BlockState getBlock(final BlockPos pos)
    {
        if (!this.inBounds(pos)) return null;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.getZ() + mob.getMin().getZ());
        return mob.getBlocks()[i][j][k];
    }

    IBlockEntity getBlockEntity();

    default World getWorld()
    {
        final Entity entity = (Entity) this.getBlockEntity();
        return entity.getCommandSenderWorld();
    }

    default TileEntity getTile(final BlockPos pos)
    {
        if (!this.inBounds(pos)) return null;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.getZ() + mob.getMin().getZ());
        final TileEntity tile = mob.getTiles()[i][j][k];
        if (tile != null) tile.setPosition(pos.immutable());
        return tile;
    }

    default boolean inBounds(final BlockPos pos)
    {
        final IBlockEntity mob = this.getBlockEntity();
        if (mob.getBlocks() == null) return false;
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.getZ() + mob.getMin().getZ());
        if (i >= mob.getBlocks().length || j >= mob.getBlocks()[0].length || k >= mob.getBlocks()[0][0].length || i < 0
                || j < 0 || k < 0)
            return false;
        return true;
    }

    default boolean setBlock(final BlockPos pos, final BlockState state)
    {
        if (!this.inBounds(pos)) return false;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.getZ() + mob.getMin().getZ());
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
            if (!entity.getCommandSenderWorld().isClientSide) entity.remove();
            return;
        }
        final int sizeX = mob.getBlocks().length;
        final int sizeY = mob.getBlocks()[0].length;
        final int sizeZ = mob.getBlocks()[0][0].length;
        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                    if (mob.getTiles()[i][j][k] != null)
                    {
                        final BlockPos pos = new BlockPos(i + xMin + entity.getX(), j + yMin + entity.getY(),
                                k + zMin + entity.getZ());
                        mob.getTiles()[i][j][k].setPosition(pos);
                        mob.getTiles()[i][j][k].clearRemoved();
                    }
    }

    default boolean setTile(final BlockPos pos, final TileEntity tile)
    {
        if (!this.inBounds(pos)) return false;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.getX() + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.getY() + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.getZ() + mob.getMin().getZ());
        mob.getTiles()[i][j][k] = tile;
        if (tile != null)
        {
            final boolean invalid = tile.isRemoved();
            if (!invalid) tile.setRemoved();
            tile.setPosition(pos.immutable());
            // TODO see about setting world for tiles.
            tile.clearRemoved();
        }
        return true;
    }

    default RayTraceResult trace(final RayTraceContext context)
    {
        return this.getWorld().clip(context);
    }
}
