package pokecube.compat.lostcities;

import mcjty.lostcities.api.ILostChunkGenerator;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.LostCityEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class Impl
{

    public static void register()
    {
        PokecubeCore.LOGGER.info("Registering Lost Cities Compat");
        TerrainSegment.defaultChecker = new LostCityTerrainChecker(TerrainSegment.defaultChecker);
        MinecraftForge.EVENT_BUS.register(Impl.class);
    }

    @SubscribeEvent
    public static void buildCity(final LostCityEvent.PostGenCityChunkEvent event)
    {

    }

    public static class LostCityTerrainChecker extends PokecubeTerrainChecker
    {
        ISubBiomeChecker parent;

        public LostCityTerrainChecker(final ISubBiomeChecker parent)
        {
            this.parent = parent;
        }

        @Override
        public int getSubBiome(final IWorld world, final Vector3 v, final TerrainSegment segment,
                final boolean caveAdjusted)
        {
            check:
            if (caveAdjusted) if (world.getChunkProvider() instanceof ServerChunkProvider)
            {
                final ChunkGenerator<?> generator = ((ServerChunkProvider) world.getChunkProvider()).generator;
                if (generator instanceof ILostChunkGenerator)
                {
                    final BlockPos pos = v.getPos();
                    final ILostChunkGenerator lostGenerator = (ILostChunkGenerator) generator;
                    final ILostChunkInfo info = lostGenerator.getChunkInfo(segment.chunkX, segment.chunkZ);
                    String type = info.getBuildingType();
                    if (!info.isCity()) break check;
                    final int streetLevel = lostGenerator.getRealHeight(info.getCityLevel());
                    int maxLevel = lostGenerator.getRealHeight(info.getNumFloors());
                    int minLevel = lostGenerator.getRealHeight(-info.getNumCellars());

                    // Adjust for streets which report funny levels.
                    if (maxLevel < streetLevel) maxLevel = streetLevel;
                    if (minLevel > streetLevel - 2) minLevel = streetLevel - 2;

                    final int diff = pos.getY() - streetLevel;
                    // Give a leeway of 5 blocks above for roof structures..
                    final boolean inStructure = pos.getY() >= minLevel && pos.getY() <= maxLevel + 5;
                    if (!inStructure) type = null;
                    // We only want streets to cover close to the ground, above
                    // that can be sky, below that can be cave.
                    else if (type == null && diff < 8 && diff > 0) type = "street";
                    if (type == null) break check;
                    return BiomeType.getBiome(type, true).getType();
                }
            }
            return super.getSubBiome(world, v, segment, caveAdjusted);
        }

    }
}
