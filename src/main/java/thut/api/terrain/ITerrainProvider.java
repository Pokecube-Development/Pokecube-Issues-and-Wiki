package thut.api.terrain;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.ThutCaps;

public interface ITerrainProvider
{
    Object lock = new Object();

    static class TerrainCache
    {
        TerrainSegment[] segs = new TerrainSegment[16];
        int              num  = 16;

        public TerrainCache(final ChunkPos temp, final IChunk chunk)
        {
            for (int i = 0; i < 16; i++)
            {
                this.segs[i] = new TerrainSegment(temp.x, i, temp.z);
                this.segs[i].chunk = chunk;
                this.segs[i].real = false;
            }
        }

        public TerrainSegment remove(final int y)
        {
            final TerrainSegment seg = this.segs[y];
            if (seg == null) return null;
            this.num--;
            return seg;
        }

        public boolean isValid()
        {
            return this.num > 0;
        }

        public TerrainSegment get(final int y)
        {
            return this.segs[y];
        }
    }

    /**
     * This is a cache of pending terrain segments, it is used as sometimes
     * segments need to have things set for them which the chunk is still being
     * generated, ie not completely loaded.
     */
    public static Map<RegistryKey<World>, Map<ChunkPos, TerrainCache>> pendingCache = new Object2ObjectOpenHashMap<>();

    /**
     * This is a cache of loaded chunks, it is used to prevent thread lock
     * contention when trying to look up a chunk, as it seems that
     * world.chunkExists returning true does not mean that you can just go and
     * ask for the chunk...
     */
    public static Map<RegistryKey<World>, Map<ChunkPos, IChunk>> loadedChunks = new Object2ObjectOpenHashMap<>();

    /**
     * Inserts the chunk into the cache of chunks.
     *
     * @param dim
     * @param chunk
     */
    public static void addChunk(final RegistryKey<World> dim, final IChunk chunk)
    {
        synchronized (ITerrainProvider.lock)
        {
            Map<ChunkPos, IChunk> chunks = ITerrainProvider.loadedChunks.getOrDefault(dim, null);
            if (chunks == null) ITerrainProvider.loadedChunks.put(dim, chunks = new Object2ObjectOpenHashMap<>());
            chunks.put(chunk.getPos(), chunk);
        }
    }

    /**
     * Removes the chunk from the cache of chunks
     *
     * @param dim
     * @param pos
     */
    public static void removeChunk(final RegistryKey<World> dim, final ChunkPos cpos)
    {
        synchronized (ITerrainProvider.lock)
        {
            final Map<ChunkPos, IChunk> chunks = ITerrainProvider.loadedChunks.get(dim);
            if (chunks != null) chunks.remove(cpos);
        }
    }

    public static IChunk getChunk(final RegistryKey<World> dim, final ChunkPos cpos)
    {
        synchronized (ITerrainProvider.lock)
        {
            final Map<ChunkPos, IChunk> chunks = ITerrainProvider.loadedChunks.get(dim);
            if (chunks == null) return null;
            return chunks.get(cpos);
        }
    }

    public static TerrainSegment removeCached(final RegistryKey<World> dim, final BlockPos pos)
    {
        final ChunkPos cpos = new ChunkPos(pos.getX(), pos.getZ());
        return ITerrainProvider.removeCached(dim, cpos, pos.getY());
    }

    public static TerrainSegment removeCached(final RegistryKey<World> dim, final ChunkPos cpos, final int y)
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
     * @param world
     *            - world like object to look up for
     * @param p
     *            - position in block coordinates, not chunk coordinates
     * @return - a terrain segement for the given position
     */
    default TerrainSegment getTerrain(final IWorld world, final BlockPos p)
    {
        if (!(world instanceof World)) return new TerrainSegment(p);
        final World rworld = (World) world;
        // Convert the pos to a chunk pos
        ChunkPos temp = null;
        int y = p.getY() >> 4;
        if (y < 0) y = 0;
        if (y > 15) y = 15;
        final RegistryKey<World> dim = rworld.getDimensionKey();
        final IChunk chunk = world.isRemote() ? world.getChunk(p)
                : ITerrainProvider.getChunk(dim, temp = new ChunkPos(p));
        final boolean real = chunk != null && chunk instanceof ICapabilityProvider;
        // This means it occurs during worldgen?
        if (!real)
        {
            Map<ChunkPos, TerrainCache> chunks = ITerrainProvider.pendingCache.get(dim);
            if (chunks == null) ITerrainProvider.pendingCache.put(dim, chunks = new Object2ObjectOpenHashMap<>());
            TerrainCache segs = chunks.get(temp);
            if (segs == null)
            {
                segs = new TerrainCache(temp, chunk);
                chunks.put(temp, segs);
            }
            return segs.get(y);
        }
        final CapabilityTerrain.ITerrainProvider provider = ((ICapabilityProvider) chunk).getCapability(
                ThutCaps.TERRAIN_CAP).orElse(null);
        provider.setChunk(chunk);
        return provider.getTerrainSegment(y);
    }
}
