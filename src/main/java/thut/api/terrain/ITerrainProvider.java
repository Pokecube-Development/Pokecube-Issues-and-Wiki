package thut.api.terrain;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.core.common.ThutCore;

public interface ITerrainProvider
{
    public static Map<BlockPos, TerrainSegment> pendingCache = Maps.newHashMap();

    default TerrainSegment getTerrain(final IWorld world, final BlockPos p)
    {
        final ChunkPos temp = new ChunkPos(p);
        final BlockPos pos = new BlockPos(temp.x, p.getY() / 16, temp.z);
        // This means it occurs during worldgen?
        if (!(world instanceof ICapabilityProvider))
        {
            /**
             * Here we need to make a new terrain segment, and cache it, then
             * later if the world is actually available, we can get the terrain
             * segment. from that.
             */
            if (ITerrainProvider.pendingCache.containsKey(pos))
            {
                ThutCore.LOGGER.debug("Cached terrain segment: {} ({})", pos, p);
                return ITerrainProvider.pendingCache.get(pos);
            }
            // No real world, so lets deal with the cache.
            final TerrainSegment segment = new TerrainSegment(pos);
            ITerrainProvider.pendingCache.put(pos, segment);
            ThutCore.LOGGER.debug("Caching terrain segment: {} ({})", pos, p);
            return segment;
        }

        final CapabilityTerrain.ITerrainProvider provider = ((ICapabilityProvider) world.getChunk(p)).getCapability(
                CapabilityTerrain.TERRAIN_CAP).orElse(null);
        if (ITerrainProvider.pendingCache.containsKey(pos))
        {
            final TerrainSegment cached = ITerrainProvider.pendingCache.remove(pos);
            // TODO if we should instead somehow merge the changes?
            provider.setTerrainSegment(cached, pos.getY());
            ThutCore.LOGGER.debug("UnCaching terrain segment: {} ({})", pos, p);
        }
        return provider.getTerrainSegement(p);
    }
}
