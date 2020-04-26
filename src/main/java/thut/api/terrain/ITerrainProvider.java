package thut.api.terrain;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface ITerrainProvider
{
    public static Map<BlockPos, TerrainSegment> pendingCache = Maps.newHashMap();

    default TerrainSegment getTerrain(final IWorld world, final BlockPos p)
    {
        final ChunkPos temp = new ChunkPos(p);
        final BlockPos pos = new BlockPos(temp.x, p.getY() / 16, temp.z);
        boolean real = world instanceof World && world.chunkExists(pos.getX() >> 4, pos.getZ() >> 4);

        IChunk chunk = null;
        if (real)
        {
            chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.SPAWN, false);
            real = chunk instanceof ICapabilityProvider;
        }

        // This means it occurs during worldgen?
        if (!real)
        {
            /**
             * Here we need to make a new terrain segment, and cache it, then
             * later if the world is actually available, we can get the terrain
             * segment. from that.
             */
            if (ITerrainProvider.pendingCache.containsKey(pos)) return ITerrainProvider.pendingCache.get(pos);
            // No real world, so lets deal with the cache.
            final TerrainSegment segment = new TerrainSegment(pos);
            segment.chunk = chunk;
            segment.real = false;
            ITerrainProvider.pendingCache.put(pos, segment);
            return segment;
        }

        final CapabilityTerrain.ITerrainProvider provider = ((ICapabilityProvider) chunk).getCapability(
                CapabilityTerrain.TERRAIN_CAP).orElse(null);
        provider.setChunk(chunk);
        if (ITerrainProvider.pendingCache.containsKey(pos))
        {
            final TerrainSegment cached = ITerrainProvider.pendingCache.remove(pos);
            // TODO if we should instead somehow merge the changes?
            provider.setTerrainSegment(cached, pos.getY());
        }
        final TerrainSegment segment = provider.getTerrainSegement(p);
        return segment;
    }
}
