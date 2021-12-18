package thut.api.entity.blockentity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.api.maths.Vector3.MutableBlockPos;

public interface IBlockEntity
{
    public static class BlockEntityFormer
    {
        public static BlockState[][][] checkBlocks(final Level world, final BlockPos min, final BlockPos max,
                final BlockPos pos)
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
            for (int i = xMin; i <= xMax; i++) for (int j = yMin; j <= yMax; j++) for (int k = zMin; k <= zMax; k++)
            {
                temp = pos.offset(i, j, k);
                final BlockState state = world.getBlockState(temp);
                if (IBlockEntity.BLOCKBLACKLIST.contains(state.getBlock().getRegistryName())) return null;
                valid = valid || !state.isAir();
                ret[i - xMin][j - yMin][k - zMin] = state;
            }
            return valid ? ret : null;
        }

        public static BlockEntity[][][] checkTiles(final Level world, final BlockPos min, final BlockPos max,
                final BlockPos pos)
        {
            final int xMin = min.getX();
            final int zMin = min.getZ();
            final int xMax = max.getX();
            final int zMax = max.getZ();
            final int yMin = min.getY();
            final int yMax = max.getY();
            final BlockEntity[][][] ret = new BlockEntity[xMax - xMin + 1][yMax - yMin + 1][zMax - zMin + 1];
            for (int i = xMin; i <= xMax; i++) for (int j = yMin; j <= yMax; j++) for (int k = zMin; k <= zMax; k++)
            {
                final BlockPos temp = pos.offset(i, j, k);
                final BlockEntity old = world.getBlockEntity(temp);
                if (old != null)
                {
                    CompoundTag tag = old.saveWithFullMetadata();
                    ret[i - xMin][j - yMin][k - zMin] = BlockEntity.loadStatic(temp, world.getBlockState(temp), tag);
                }
            }
            return ret;
        }

        public static <T extends Entity> T makeBlockEntity(final Level world, BlockPos min, BlockPos max,
                final BlockPos pos, final EntityType<T> type)
        {
            final T ret = type.create(world);
            // This enforces that min is the lower corner, and max is the upper.
            final AABB box = new AABB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            final IBlockEntity entity = (IBlockEntity) ret;
            ret.setPos(pos.getX(), pos.getY(), pos.getZ());
            final BlockState[][][] blocks = BlockEntityFormer.checkBlocks(world, min, max, pos);
            if (blocks == null) return null;
            entity.setBlocks(blocks);
            entity.setTiles(BlockEntityFormer.checkTiles(world, min, max, pos));
            entity.setMin(min);
            entity.setMax(max);
            BlockEntityFormer.removeBlocks(world, min, max, pos);
            world.addFreshEntity(ret);
            return ret;
        }

        public static HitResult rayTraceInternal(final Vec3 start, final Vec3 end, final IBlockEntity toTrace)
        {
            Vec3 diff = end.subtract(start);
            final double l = diff.length();
            diff = diff.normalize();
            final IBlockEntityWorld world = toTrace.getFakeWorld();
            final MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
            for (double i = 0; i < l; i += 0.1)
            {
                final Vec3 spot = start.add(diff.multiply(i, i, i));
                pos.set(Mth.floor(spot.x), Mth.floor(spot.y), Mth.floor(spot.z));
                final BlockState state = world.getBlock(pos);
                if (state != null && !world.isEmptyBlock(pos))
                {
                    final VoxelShape shape = state.getCollisionShape(world, pos);
                    final BlockHitResult hit = shape.clip(start, end, pos);
                    if (hit != null) return hit;
                }
            }
            return BlockHitResult.miss(end, Direction.DOWN, new BlockPos(end));
        }

        public static void removeBlocks(final Level world, final BlockPos min, final BlockPos max, final BlockPos pos)
        {
            final int xMin = min.getX();
            final int zMin = min.getZ();
            final int xMax = max.getX();
            final int zMax = max.getZ();
            final int yMin = min.getY();
            final int yMax = max.getY();
            final BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos();
            for (int i = xMin; i <= xMax; i++) for (int j = yMin; j <= yMax; j++) for (int k = zMin; k <= zMax; k++)
            {
                temp.set(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                final BlockEntity tile = world.getBlockEntity(temp);
                ITileRemover tileHandler = null;
                if (tile != null)
                {
                    tileHandler = IBlockEntity.getRemover(tile);
                    tileHandler.preBlockRemoval(tile);
                }
            }
            for (int i = xMin; i <= xMax; i++) for (int j = yMin; j <= yMax; j++) for (int k = zMin; k <= zMax; k++)
            {
                temp.set(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                final BlockEntity tile = world.getBlockEntity(temp);
                ITileRemover tileHandler = null;
                if (tile != null) tileHandler = IBlockEntity.getRemover(tile);
                world.setBlock(temp, Blocks.AIR.defaultBlockState(), 2 + 16 + 32 + 64);
                if (tileHandler != null) tileHandler.postBlockRemoval(tile);
            }
            for (int i = xMin; i <= xMax; i++) for (int j = yMin; j <= yMax; j++) for (int k = zMin; k <= zMax; k++)
            {
                temp.set(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                world.setBlock(temp, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        public static void RevertEntity(final IBlockEntity toRevert)
        {
            final int xMin = toRevert.getMin().getX();
            final int zMin = toRevert.getMin().getZ();
            final int yMin = toRevert.getMin().getY();
            if (toRevert.getBlocks() == null) return;
            final int sizeX = toRevert.getBlocks().length;
            final int sizeY = toRevert.getBlocks()[0].length;
            final int sizeZ = toRevert.getBlocks()[0][0].length;
            final Entity entity = (Entity) toRevert;
            for (int i = 0; i < sizeX; i++) for (int j = 0; j < sizeY; j++) for (int k = 0; k < sizeZ; k++)
            {
                // TODO Apply transformation onto this pos based on
                // whether the entity is rotated, and then also call the
                // block's rotate method as well before placing the
                // BlockState.
                final BlockPos pos = new BlockPos(i + xMin + entity.getX(), j + yMin + entity.getY(),
                        k + zMin + entity.getZ());
                final BlockState state = toRevert.getFakeWorld().getBlock(pos);
                final BlockEntity tile = toRevert.getFakeWorld().getTile(pos);
                if (state != null)
                {
                    if (!entity.getCommandSenderWorld().isEmptyBlock(pos))
                        entity.getCommandSenderWorld().destroyBlock(pos, true);
                    entity.getCommandSenderWorld().setBlockAndUpdate(pos, state);
                    if (tile != null)
                    {
                        final BlockEntity newTile = entity.getCommandSenderWorld().getBlockEntity(pos);
                        if (newTile != null) newTile.load(tile.save(new CompoundTag()));
                    }
                }
            }
            final List<Entity> possibleInside = entity.getCommandSenderWorld().getEntities(entity,
                    entity.getBoundingBox());
            for (final Entity e : possibleInside) e.setPos(e.getX(), e.getY() + 0.25, e.getZ());
        }
    }

    public static interface ITileRemover
    {
        default int getPriority()
        {
            return 0;
        }

        void postBlockRemoval(BlockEntity tileIn);

        void preBlockRemoval(BlockEntity tileIn);
    }

    static Set<ResourceLocation> BLOCKBLACKLIST = Sets.newHashSet();
    static Set<String> TEBLACKLIST = Sets.newHashSet();

    static BiMap<Class<?>, ITileRemover> CUSTOMREMOVERS = HashBiMap.create();

    List<ITileRemover> SORTEDREMOVERS = Lists.newArrayList();

    static final ITileRemover DEFAULTREMOVER = new ITileRemover()
    {

        @Override
        public void postBlockRemoval(final BlockEntity tileIn)
        {}

        @Override
        public void preBlockRemoval(final BlockEntity tileIn)
        {
            tileIn.setRemoved();
        }
    };

    public static void addRemover(final ITileRemover remover, final Class<?> clas)
    {
        IBlockEntity.CUSTOMREMOVERS.put(clas, remover);
        IBlockEntity.SORTEDREMOVERS.add(remover);
        Collections.sort(IBlockEntity.SORTEDREMOVERS, (o1, o2) -> o1.getPriority() - o2.getPriority());
    }

    public static ITileRemover getRemover(final BlockEntity tile)
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

    IBlockEntityWorld getFakeWorld();

    BlockEntityInteractHandler getInteractor();

    BlockPos getMax();

    BlockPos getMin();

    default BlockPos getSize()
    {
        return this.getMax().subtract(this.getMin());
    }

    BlockPos getOriginalPos();

    BlockEntity[][][] getTiles();

    void setBlocks(BlockState[][][] blocks);

    void setFakeWorld(IBlockEntityWorld world);

    void setMax(BlockPos pos);

    void setMin(BlockPos pos);

    void setSize(EntityDimensions size);

    void setTiles(BlockEntity[][][] tiles);

    default boolean shouldHide(final BlockPos pos)
    {
        final BlockEntity tile = this.getFakeWorld().getTile(pos);
        if (tile != null && !BlockEntityUpdater.isWhitelisted(tile)) return true;
        return false;
    }

}
