package thut.api.terrain;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.ThutCaps;

public interface ITerrainProvider
{
    Object lock = new Object();

    /**
     * This is a cache of pending terrain segments, it is used as sometimes
     * segments need to have things set for them which the chunk is still being
     * generated, ie not completely loaded.
     */
    public static Map<DimensionType, Map<BlockPos, TerrainSegment>> pendingCache = new Object2ObjectOpenHashMap<>();

    /**
     * This is a cache of loaded chunks, it is used to prevent thread lock
     * contention when trying to look up a chunk, as it seems that
     * world.chunkExists returning true does not mean that you can just go and
     * ask for the chunk...
     */
    public static Map<GlobalChunkPos, IChunk> loadedChunks = new Object2ObjectOpenHashMap<>();

    /**
     * Inserts the chunk into the cache of chunks.
     *
     * @param dim
     * @param chunk
     */
    public static void addChunk(final DimensionType dim, final IChunk chunk)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, chunk.getPos());
        synchronized (ITerrainProvider.lock)
        {
            ITerrainProvider.loadedChunks.put(pos, chunk);
        }
    }

    /**
     * Removes the chunk from the cache of chunks
     *
     * @param dim
     * @param pos
     */
    public static void removeChunk(final DimensionType dim, final ChunkPos cpos)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, cpos);
        synchronized (ITerrainProvider.lock)
        {
            ITerrainProvider.loadedChunks.remove(pos);
        }
    }

    public static IChunk getChunk(final DimensionType dim, final ChunkPos cpos)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, cpos);
        return ITerrainProvider.loadedChunks.get(pos);
    }

    public static TerrainSegment removeCached(final DimensionType dim, final BlockPos pos)
    {
        return ITerrainProvider.pendingCache.getOrDefault(dim, Collections.emptyMap()).remove(pos);
    }

    /**
     * @param world
     *            - world like object to look up for
     * @param p
     *            - position in block coordinates, not chunk coordinates
     * @return - a terrain segement for the given position
     */
    default TerrainSegment getTerrain(final IWorld world, final BlockPos p)
    {
        // Convert the pos to a chunk pos
        final ChunkPos temp = new ChunkPos(p);
        // Include the value for y
        final BlockPos pos = new BlockPos(temp.x, p.getY() / 16, temp.z);
        final DimensionType dim = world.getDimension().getType();
        final IChunk chunk = ITerrainProvider.getChunk(dim, temp);
        final boolean real = chunk != null && chunk instanceof ICapabilityProvider;
        // This means it occurs during worldgen?
        if (!real)
        {
            final Map<BlockPos, TerrainSegment> dimMap = ITerrainProvider.pendingCache.getOrDefault(dim, Maps
                    .newConcurrentMap());
            /**
             * Here we need to make a new terrain segment, and cache it, then
             * later if the world is actually available, we can get the terrain
             * segment. from that.
             */
            if (dimMap.containsKey(pos)) return dimMap.get(pos);
            // No real world, so lets deal with the cache.
            final TerrainSegment segment = new TerrainSegment(pos);
            segment.chunk = chunk;
            segment.real = false;
            dimMap.put(pos, segment);
            if (!ITerrainProvider.pendingCache.containsKey(dim)) ITerrainProvider.pendingCache.put(dim, dimMap);
            return segment;
        }

        final CapabilityTerrain.ITerrainProvider provider = ((ICapabilityProvider) chunk).getCapability(
                ThutCaps.TERRAIN_CAP).orElse(null);
        provider.setChunk(chunk);
        return provider.getTerrainSegement(pos);
    }
}
