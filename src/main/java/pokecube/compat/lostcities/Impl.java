package pokecube.compat.lostcities;

import java.util.Locale;

import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import pokecube.core.PokecubeCore;
import pokecube.core.events.StructureEvent.PickLocation;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class Impl
{
    private static boolean reged = false;

    public static void register()
    {
        if (Impl.reged) return;
        Impl.reged = true;
        PokecubeCore.LOGGER.info("Registering Lost Cities Compat.");
        TerrainSegment.defaultChecker = new LostCityTerrainChecker(TerrainSegment.defaultChecker);
        MinecraftForge.EVENT_BUS.register(Impl.class);
    }

    @SubscribeEvent
    public static void buildStructure(final PickLocation event)
    {
        final ChunkGenerator<?> generator = event.chunkGen;
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        final ServerWorld world = server.getWorld(generator.world.getDimension().getType());
        final IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.getDimensionInfo(world);
        if (dimInfo == null)
        {
            PokecubeCore.LOGGER.debug("Null dimInfo, can't check for structure!");
            return;
        }
        final int r = 1;
        for (int i = -r; i <= r; i++)
            for (int j = -r; j <= r; j++)
                if (BuildingInfo.isCity(event.chunkPosX + i, event.chunkPosZ + j, dimInfo))
                {
                    event.setCanceled(true);
                    return;
                }
    }

    public static class LostCityTerrainChecker extends PokecubeTerrainChecker
    {
        ISubBiomeChecker parent;

        public LostCityTerrainChecker(final ISubBiomeChecker parent)
        {
            this.parent = parent;
        }

        @Override
        public int getSubBiome(final IWorld world_in, final Vector3 v, final TerrainSegment segment,
                final boolean caveAdjusted)
        {
            check:
            if (caveAdjusted) if (world_in.getChunkProvider() instanceof ServerChunkProvider)
            {
                final ServerWorld world = ((ServerChunkProvider) world_in.getChunkProvider()).world;
                final IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.getDimensionInfo(world);
                if (dimInfo == null) break check;
                final BlockPos pos = v.getPos();
                final ChunkPos pos_chunk = new ChunkPos(pos);
                final BuildingInfo info = BuildingInfo.getBuildingInfo(pos_chunk.x, pos_chunk.z, dimInfo);
                if (info == null) break check;
                if (!info.isCity()) break check;

                final int streetLevel = info.getCityGroundLevel();
                int maxLevel = info.getMaxHeight();
                int minLevel = info.groundLevel + 6 * -info.getNumCellars();

                // Adjust for streets which report funny levels.
                if (maxLevel < streetLevel) maxLevel = streetLevel;
                if (minLevel > streetLevel - 2) minLevel = streetLevel - 2;

                final int diff = pos.getY() - streetLevel;
                // Give a leeway of 5 blocks above for roof structures..
                final boolean inStructure = pos.getY() >= minLevel && pos.getY() <= maxLevel + 5;

                String type = info.getBuildingType();
                if (!inStructure) type = null;
                // We only want streets to cover close to the ground, above
                // that can be sky, below that can be cave.
                else if (type == null && diff < 8 && diff > 0) type = "street";
                if (type == null) break check;
                type = type.toLowerCase(Locale.ROOT);
                if (PokecubeTerrainChecker.structureSubbiomeMap.containsKey(type))
                    type = PokecubeTerrainChecker.structureSubbiomeMap.get(type);
                return BiomeType.getBiome(type, true).getType();
            }
            return super.getSubBiome(world_in, v, segment, caveAdjusted);
        }

    }
}
