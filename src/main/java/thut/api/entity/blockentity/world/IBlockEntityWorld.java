package thut.api.entity.blockentity.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import thut.api.entity.blockentity.IBlockEntity;

public interface IBlockEntityWorld extends IBlockReader
{
    default BlockState getBlock(final BlockPos pos)
    {
        if (!this.inBounds(pos)) return null;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.posX + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.posY + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.posZ + mob.getMin().getZ());
        return mob.getBlocks()[i][j][k];
    }

    IBlockEntity getBlockEntity();

    default World getWorld()
    {
        final Entity entity = (Entity) this.getBlockEntity();
        return entity.getEntityWorld();
    }

    default TileEntity getTile(final BlockPos pos)
    {
        if (!this.inBounds(pos)) return null;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.posX + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.posY + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.posZ + mob.getMin().getZ());
        final TileEntity tile = mob.getTiles()[i][j][k];
        if (tile != null)
        {
            // TODO see about setting world for tiles.
            final boolean invalid = tile.isRemoved();
            if (!invalid) tile.remove();
            tile.setPos(pos.toImmutable());
        }
        return tile;
    }

    default boolean inBounds(final BlockPos pos)
    {
        final IBlockEntity mob = this.getBlockEntity();
        if (mob.getBlocks() == null) return false;
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.posX + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.posY + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.posZ + mob.getMin().getZ());
        if (i >= mob.getBlocks().length || j >= mob.getBlocks()[0].length || k >= mob.getBlocks()[0][0].length || i < 0
                || j < 0 || k < 0) return false;
        return true;
    }

    default boolean setBlock(final BlockPos pos, final BlockState state)
    {
        if (!this.inBounds(pos)) return false;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.posX + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.posY + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.posZ + mob.getMin().getZ());
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
            if (!entity.getEntityWorld().isRemote) entity.remove();
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
                        final BlockPos pos = new BlockPos(i + xMin + entity.posX, j + yMin + entity.posY, k + zMin
                                + entity.posZ);
                        mob.getTiles()[i][j][k].setPos(pos);
                        mob.getTiles()[i][j][k].validate();
                        // TODO see about setting world for tiles.
                    }
    }

    default boolean setTile(final BlockPos pos, final TileEntity tile)
    {
        if (!this.inBounds(pos)) return false;
        final IBlockEntity mob = this.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.posX + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.posY + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.posZ + mob.getMin().getZ());
        mob.getTiles()[i][j][k] = tile;
        if (tile != null)
        {
            final boolean invalid = tile.isRemoved();
            if (!invalid) tile.remove();
            tile.setPos(pos.toImmutable());
            // TODO see about setting world for tiles.
            tile.validate();
        }
        return true;
    }

    default RayTraceResult trace(final RayTraceContext context)
    {
        return this.getWorld().rayTraceBlocks(context);
    }
}
