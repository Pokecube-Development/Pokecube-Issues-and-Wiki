package thut.api.entity.blockentity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thut.api.entity.blockentity.world.client.IBlockEntityWorld;

public interface IBlockEntity
{
    public static class BlockEntityFormer
    {
        public static BlockState[][][] checkBlocks(World world, BlockPos min, BlockPos max, BlockPos pos)
        {
            final int xMin = min.getX();
            final int zMin = min.getZ();
            final int xMax = max.getX();
            final int zMax = max.getZ();
            final int yMin = min.getY();
            final int yMax = max.getY();
            final BlockState[][][] ret = new BlockState[xMax - xMin + 1][yMax - yMin + 1][zMax - zMin + 1];
            boolean valid = false;
            BlockPos temp;
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        temp = pos.add(i, j, k);
                        final BlockState state = world.getBlockState(temp);
                        if (IBlockEntity.BLOCKBLACKLIST.contains(state.getBlock().getRegistryName())) return null;
                        valid = valid || !state.getBlock().isAir(state, world, pos);
                        ret[i - xMin][j - yMin][k - zMin] = state;
                    }
            return valid ? ret : null;
        }

        public static TileEntity[][][] checkTiles(World world, BlockPos min, BlockPos max, BlockPos pos)
        {
            final int xMin = min.getX();
            final int zMin = min.getZ();
            final int xMax = max.getX();
            final int zMax = max.getZ();
            final int yMin = min.getY();
            final int yMax = max.getY();
            final TileEntity[][][] ret = new TileEntity[xMax - xMin + 1][yMax - yMin + 1][zMax - zMin + 1];
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        final BlockPos temp = pos.add(i, j, k);
                        final TileEntity old = world.getTileEntity(temp);
                        if (old != null)
                        {
                            CompoundNBT tag = new CompoundNBT();
                            tag = old.write(tag);
                            ret[i - xMin][j - yMin][k - zMin] = TileEntity.create(tag);
                        }
                    }
            return ret;
        }

        public static <T extends Entity> T makeBlockEntity(World world, BlockPos min, BlockPos max, BlockPos pos,
                EntityType<T> type)
        {
            final T ret = type.create(world);
            // This enforces that min is the lower corner, and max is the upper.
            final AxisAlignedBB box = new AxisAlignedBB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            final IBlockEntity entity = (IBlockEntity) ret;
            ret.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            final BlockState[][][] blocks = BlockEntityFormer.checkBlocks(world, min, max, pos);
            if (blocks == null) return null;
            entity.setBlocks(blocks);
            entity.setTiles(BlockEntityFormer.checkTiles(world, min, max, pos));
            entity.setMin(min);
            entity.setMax(max);
            BlockEntityFormer.removeBlocks(world, min, max, pos);
            world.addEntity(ret);
            return ret;
        }

        public static RayTraceResult rayTraceInternal(Vec3d start, Vec3d end, IBlockEntity toTrace)
        {
            final RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER,
                    RayTraceContext.FluidMode.NONE, (Entity) toTrace);
            return toTrace.getFakeWorld().trace(context);
        }

        public static void removeBlocks(World world, BlockPos min, BlockPos max, BlockPos pos)
        {
            final int xMin = min.getX();
            final int zMin = min.getZ();
            final int xMax = max.getX();
            final int zMax = max.getZ();
            final int yMin = min.getY();
            final int yMax = max.getY();
            final BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos();
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        temp.setPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                        final TileEntity tile = world.getTileEntity(temp);
                        ITileRemover tileHandler = null;
                        if (tile != null)
                        {
                            tileHandler = IBlockEntity.getRemover(tile);
                            tileHandler.preBlockRemoval(tile);
                        }
                    }
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        temp.setPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                        final TileEntity tile = world.getTileEntity(temp);
                        ITileRemover tileHandler = null;
                        if (tile != null) tileHandler = IBlockEntity.getRemover(tile);
                        world.setBlockState(temp, Blocks.AIR.getDefaultState(), 2);
                        if (tileHandler != null) tileHandler.postBlockRemoval(tile);
                    }
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        temp.setPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                        world.setBlockState(temp, Blocks.AIR.getDefaultState(), 3);
                    }
        }

        public static void RevertEntity(IBlockEntity toRevert)
        {
            final int xMin = toRevert.getMin().getX();
            final int zMin = toRevert.getMin().getZ();
            final int yMin = toRevert.getMin().getY();
            if (toRevert.getBlocks() == null) return;
            final int sizeX = toRevert.getBlocks().length;
            final int sizeY = toRevert.getBlocks()[0].length;
            final int sizeZ = toRevert.getBlocks()[0][0].length;
            final Entity entity = (Entity) toRevert;
            for (int i = 0; i < sizeX; i++)
                for (int j = 0; j < sizeY; j++)
                    for (int k = 0; k < sizeZ; k++)
                    {
                        // TODO Apply transformation onto this pos based on
                        // whether the entity is rotated, and then also call the
                        // block's rotate method as well before placing the
                        // BlockState.
                        final BlockPos pos = new BlockPos(i + xMin + entity.posX, j + yMin + entity.posY, k + zMin
                                + entity.posZ);
                        final BlockState state = toRevert.getFakeWorld().getBlock(pos);
                        final TileEntity tile = toRevert.getFakeWorld().getTile(pos);
                        if (state != null)
                        {
                            entity.getEntityWorld().setBlockState(pos, state);
                            if (tile != null)
                            {
                                final TileEntity newTile = entity.getEntityWorld().getTileEntity(pos);
                                if (newTile != null) newTile.read(tile.write(new CompoundNBT()));
                            }
                        }
                    }
            final List<Entity> possibleInside = entity.getEntityWorld().getEntitiesWithinAABBExcludingEntity(entity,
                    entity.getBoundingBox());
            for (final Entity e : possibleInside)
                e.setPosition(e.posX, e.posY + 0.25, e.posZ);
        }
    }

    public static interface ITileRemover
    {
        default int getPriority()
        {
            return 0;
        }

        void postBlockRemoval(TileEntity tileIn);

        void preBlockRemoval(TileEntity tileIn);
    }

    static Set<ResourceLocation> BLOCKBLACKLIST = Sets.newHashSet();
    static Set<String>           TEBLACKLIST    = Sets.newHashSet();

    static BiMap<Class<?>, ITileRemover> CUSTOMREMOVERS = HashBiMap.create();

    List<ITileRemover> SORTEDREMOVERS = Lists.newArrayList();

    static final ITileRemover DEFAULTREMOVER = new ITileRemover()
    {

        @Override
        public void postBlockRemoval(TileEntity tileIn)
        {
        }

        @Override
        public void preBlockRemoval(TileEntity tileIn)
        {
            tileIn.remove();
        }
    };

    public static void addRemover(ITileRemover remover, Class<?> clas)
    {
        IBlockEntity.CUSTOMREMOVERS.put(clas, remover);
        IBlockEntity.SORTEDREMOVERS.add(remover);
        Collections.sort(IBlockEntity.SORTEDREMOVERS, (o1, o2) -> o1.getPriority() - o2.getPriority());
    }

    public static ITileRemover getRemover(TileEntity tile)
    {
        final ITileRemover ret = IBlockEntity.CUSTOMREMOVERS.get(tile.getClass());
        if (ret != null) return ret;
        for (final ITileRemover temp : IBlockEntity.SORTEDREMOVERS)
        {
            final Class<?> key = IBlockEntity.CUSTOMREMOVERS.inverse().get(temp);
            if (key.isInstance(tile)) return temp;
        }
        return IBlockEntity.DEFAULTREMOVER;
    }

    BlockState[][][] getBlocks();

    IBlockEntityWorld<?> getFakeWorld();

    BlockEntityInteractHandler getInteractor();

    BlockPos getMax();

    BlockPos getMin();

    TileEntity[][][] getTiles();

    void setBlocks(BlockState[][][] blocks);

    void setFakeWorld(IBlockEntityWorld<?> world);

    void setMax(BlockPos pos);

    void setMin(BlockPos pos);

    void setSize(EntitySize size);

    void setTiles(TileEntity[][][] tiles);

    default boolean shouldHide(BlockPos pos)
    {
        final TileEntity tile = this.getFakeWorld().getTile(pos);
        if (tile != null && !BlockEntityUpdater.isWhitelisted(tile)) return true;
        return false;
    }

}
