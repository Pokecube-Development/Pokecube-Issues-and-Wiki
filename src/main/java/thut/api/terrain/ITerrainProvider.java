package thut.api.terrain;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface ITerrainProvider
{
    default TerrainSegment getTerrain(final World world, final BlockPos p)
    {
        final CapabilityTerrain.ITerrainProvider provider = ((ICapabilityProvider) world.getChunk(p)).getCapability(
                CapabilityTerrain.TERRAIN_CAP).orElse(null);
        return provider.getTerrainSegement(p);
    }
}
