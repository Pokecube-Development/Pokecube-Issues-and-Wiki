package thut.api.level.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import thut.api.ThutCaps;
import thut.core.common.ThutCore;

public interface ITerrainProvider
{
    /**
     * @param world - world like object to look up for
     * @param p     - position in block coordinates, not chunk coordinates
     * @return - a terrain segement for the given position
     */
    default TerrainSegment getTerrain(final LevelAccessor world, final BlockPos p)
    {
        if (!(world instanceof Level level))
        {
            ThutCore.LOGGER.error("ERROR, Umloaded chunk", new IllegalStateException());
            return new TerrainSegment(p);
        }
        // Convert the pos to a chunk pos
        int x = SectionPos.blockToSectionCoord(p.getX());
        int z = SectionPos.blockToSectionCoord(p.getZ());
        var getter = level.getChunk(x, z, ChunkStatus.LIGHT, false);
        if(!(getter instanceof ChunkAccess chunk))
        {
            ThutCore.LOGGER.error("ERROR, Umloaded chunk", new IllegalStateException());
            return new TerrainSegment(p);
        }

        int y = SectionPos.blockToSectionCoord(p.getY());
        if (y < world.getMinSection()) y = world.getMinSection();
        if (y > world.getMaxSection()) y = world.getMaxSection();

        final CapabilityTerrain.ITerrainProvider provider = ThutCaps.getTerrainProvider(chunk);
        if (provider == null)
        {
            Thread.dumpStack();
        }
        return provider.setChunk(chunk).getTerrainSegment(y);
    }
}
