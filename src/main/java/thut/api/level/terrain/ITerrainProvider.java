package thut.api.level.terrain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.ThutCaps;

public interface ITerrainProvider
{
    static class TerrainCache
    {
        Int2ObjectArrayMap<TerrainSegment> segMap = new Int2ObjectArrayMap<>();

        int num;

        ChunkPos pos;
        ChunkAccess chunk;

        public TerrainCache(final ChunkPos temp, final ChunkAccess chunk, final LevelAccessor world)
        {
            this.pos = temp;
            this.chunk = chunk;
        }

        public TerrainSegment remove(final int y)
        {
            final TerrainSegment seg = this.segMap.get(y);
            if (seg == null) return null;
            this.num--;
            return seg;
        }

        public boolean isValid()
        {
            return this.num > 0;
        }

        private TerrainSegment make(int y)
        {
            final TerrainSegment seg = new TerrainSegment(this.pos.x, y, this.pos.z);
            seg.chunk = chunk;
            seg.real = false;
            this.num++;
            return seg;
        }

        public TerrainSegment get(final int y)
        {
            return this.segMap.computeIfAbsent(y, this::make);
        }
    }

    /**
     * This is a cache of pending terrain segments, it is used as sometimes
     * segments need to have things set for them which the chunk is still being
     * generated, ie not completely loaded.
     */
    public static Map<ResourceKey<Level>, Map<ChunkPos, TerrainCache>> pendingCache = new ConcurrentHashMap<>();

    /**
     * This is a cache of loaded chunks, it is used to prevent thread lock
     * contention when trying to look up a chunk, as it seems that
     * world.chunkExists returning true does not mean that you can just go and
     * ask for the chunk...
     */
    public static Map<ResourceKey<Level>, Map<ChunkPos, ChunkAccess>> loadedChunks = new ConcurrentHashMap<>();

    /**
     * Inserts the chunk into the cache of chunks.
     *
     * @param dim
     * @param chunk
     */
    public static void addChunk(final ResourceKey<Level> dim, final ChunkAccess chunk)
    {
        Map<ChunkPos, ChunkAccess> chunks = ITerrainProvider.loadedChunks.getOrDefault(dim, null);
        if (chunks == null) ITerrainProvider.loadedChunks.put(dim, chunks = new ConcurrentHashMap<>());
        chunks.put(chunk.getPos(), chunk);
    }

    /**
     * Removes the chunk from the cache of chunks
     *
     * @param dim
     * @param pos
     */
    public static void removeChunk(final ResourceKey<Level> dim, final ChunkPos cpos)
    {
        final Map<ChunkPos, ChunkAccess> chunks = ITerrainProvider.loadedChunks.get(dim);
        if (chunks != null) chunks.remove(cpos);
    }

    public static ChunkAccess getChunk(final ResourceKey<Level> dim, final ChunkPos cpos)
    {
        final Map<ChunkPos, ChunkAccess> chunks = ITerrainProvider.loadedChunks.get(dim);
        if (chunks == null) return null;
        return chunks.get(cpos);
    }

    public static TerrainSegment removeCached(final ResourceKey<Level> dim, final BlockPos pos)
    {
        final ChunkPos cpos = new ChunkPos(pos.getX(), pos.getZ());
        return ITerrainProvider.removeCached(dim, cpos, pos.getY());
    }

    public static TerrainSegment removeCached(final ResourceKey<Level> dim, final ChunkPos cpos, final int y)
    {
        final Map<ChunkPos, TerrainCache> chunks = ITerrainProvider.pendingCache.get(dim);
        if (chunks == null) return null;
        final TerrainCache segs = chunks.get(cpos);
        if (segs == null) return null;
        final TerrainSegment var = segs.remove(y);
        if (!segs.isValid()) chunks.remove(cpos);
        return var;
    }

    /**
     * @param world - world like object to look up for
     * @param p     - position in block coordinates, not chunk coordinates
     * @return - a terrain segement for the given position
     */
    default TerrainSegment getTerrain(final LevelAccessor world, final BlockPos p)
    {
        if (!(world instanceof Level level)) return new TerrainSegment(p);
        // Convert the pos to a chunk pos
        ChunkPos temp = null;
        final ResourceKey<Level> dim = level.dimension();
        final ChunkAccess chunk = world.isClientSide() ? world.getChunk(p)
                : ITerrainProvider.getChunk(dim, temp = new ChunkPos(p));
        // final ChunkAccess chunk = world.getChunk(p);
        // temp = chunk.getPos();
        final boolean real = chunk != null && chunk instanceof ICapabilityProvider;

        int y = SectionPos.blockToSectionCoord(p.getY());
        if (y < world.getMinSection()) y = world.getMinSection();
        if (y > world.getMaxSection()) y = world.getMaxSection();

        // This means it occurs during worldgen?
        if (!real)
        {
            Map<ChunkPos, TerrainCache> chunks = ITerrainProvider.pendingCache.get(dim);
            if (chunks == null) ITerrainProvider.pendingCache.put(dim, chunks = new ConcurrentHashMap<>());
            TerrainCache segs = chunks.get(temp);
            if (segs == null)
            {
                segs = new TerrainCache(temp, chunk, world);
                chunks.put(temp, segs);
            }
            return segs.get(y);
        }
        final CapabilityTerrain.ITerrainProvider provider = ((ICapabilityProvider) chunk)
                .getCapability(ThutCaps.TERRAIN_PROVIDER).orElse(null);
        provider.setChunk(chunk);
        return provider.getTerrainSegment(y);
    }
}
