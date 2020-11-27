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
    public static Map<GlobalChunkPos, TerrainCache> pendingCache = new Object2ObjectOpenHashMap<>();

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
    public static void addChunk(final RegistryKey<World> dim, final IChunk chunk)
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
    public static void removeChunk(final RegistryKey<World> dim, final ChunkPos cpos)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, cpos);
        synchronized (ITerrainProvider.lock)
        {
            if (ITerrainProvider.loadedChunks.containsKey(pos)) ITerrainProvider.loadedChunks.remove(pos);
        }
    }

    public static IChunk getChunk(final RegistryKey<World> dim, final ChunkPos cpos)
    {
        final GlobalChunkPos pos = new GlobalChunkPos(dim, cpos);
        synchronized (ITerrainProvider.lock)
        {
            if (!ITerrainProvider.loadedChunks.containsKey(pos)) return null;
            return ITerrainProvider.loadedChunks.get(pos);
        }
    }

    public static TerrainSegment removeCached(final RegistryKey<World> dim, final BlockPos pos)
    {
        final GlobalChunkPos wpos = new GlobalChunkPos(dim, new ChunkPos(pos.getX(), pos.getZ()));
        final TerrainCache segs = ITerrainProvider.pendingCache.get(wpos);
        if (segs == null) return null;
        final TerrainSegment var = segs.remove(pos.getY());
        if (!segs.isValid()) ITerrainProvider.pendingCache.remove(wpos);
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
        if(!(world instanceof World)) return new TerrainSegment(p);
        final World rworld = (World)world;
        // Convert the pos to a chunk pos
        final ChunkPos temp = new ChunkPos(p);
        int y = p.getY() >> 4;
        if (y < 0) y = 0;
        if (y > 15) y = 15;
        // Include the value for y
        final BlockPos pos = new BlockPos(temp.x, y, temp.z);
        final RegistryKey<World> dim = rworld.getDimensionKey();
        final IChunk chunk = ITerrainProvider.getChunk(dim, temp);
        final boolean real = chunk != null && chunk instanceof ICapabilityProvider;
        // This means it occurs during worldgen?
        if (!real)
        {
            final GlobalChunkPos wpos = new GlobalChunkPos(dim, temp);
            TerrainCache segs = ITerrainProvider.pendingCache.get(wpos);
            if (segs == null)
            {
                segs = new TerrainCache(temp, chunk);
                ITerrainProvider.pendingCache.put(wpos, segs);
            }
            return segs.get(y);
        }
        final CapabilityTerrain.ITerrainProvider provider = ((ICapabilityProvider) chunk).getCapability(
                ThutCaps.TERRAIN_CAP).orElse(null);
        provider.setChunk(chunk);
        return provider.getTerrainSegement(pos);
    }
}
