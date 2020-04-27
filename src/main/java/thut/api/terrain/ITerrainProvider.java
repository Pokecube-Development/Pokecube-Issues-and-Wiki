package thut.api.terrain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.ThutCaps;

public interface ITerrainProvider
{
    public static Map<DimensionType, Map<BlockPos, TerrainSegment>> pendingCache = Maps.newHashMap();

    public static TerrainSegment removeCached(final DimensionType dim, final BlockPos pos)
    {
        return ITerrainProvider.pendingCache.getOrDefault(dim, Collections.emptyMap()).remove(pos);
    }

    default TerrainSegment getTerrain(final IWorld world, final BlockPos p)
    {
        final ChunkPos temp = new ChunkPos(p);
        final BlockPos pos = new BlockPos(temp.x, p.getY() / 16, temp.z);
        final DimensionType dim = world.getDimension().getType();
        boolean real = world instanceof World && world.chunkExists(pos.getX(), pos.getZ());
        Map<BlockPos, TerrainSegment> dimMap = null;
        IChunk chunk = null;
        if (real)
        {
            chunk = world.getChunk(pos.getX(), pos.getZ(), ChunkStatus.FULL, false);
            real = chunk instanceof ICapabilityProvider;
        }

        // This means it occurs during worldgen?
        if (!real)
        {
            dimMap = ITerrainProvider.pendingCache.getOrDefault(dim, new HashMap<>());
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
