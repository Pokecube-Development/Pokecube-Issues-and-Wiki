package pokecube.compat.lostcities;

import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;
import pokecube.core.PokecubeCore;
import pokecube.core.events.StructureEvent.PickLocation;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class Impl
{
    private static Set<String> logged = Sets.newHashSet();

    public static void register()
    {
        PokecubeCore.LOGGER.info("Registering Lost Cities Compat.");
        TerrainSegment.defaultChecker = new LostCityTerrainChecker(TerrainSegment.defaultChecker);
        MinecraftForge.EVENT_BUS.register(Impl.class);
    }

    @SubscribeEvent
    public static void buildStructure(final PickLocation event)
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        final ServerLevel world = server.getLevel(event.getDimensionKey());
        final IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.getDimensionInfo(world);
        if (dimInfo == null)
        {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Null dimInfo, can't check for structure!");
            return;
        }
        final int r = 1;
        try
        {
            for (int i = -r; i <= r; i++)
                for (int j = -r; j <= r; j++)
                    if (BuildingInfo.isCity(event.chunkPosX + i, event.chunkPosZ + j, dimInfo))
                    {
                        event.setCanceled(true);
                        return;
                    }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error checking for lost cities structure!", e);
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
        public BiomeType getSubBiome(final LevelAccessor world_in, final Vector3 v, final TerrainSegment segment,
                final boolean caveAdjusted)
        {
            check:
            if (caveAdjusted) if (world_in.getChunkSource() instanceof ServerChunkCache)
            {
                final ServerLevel world = ((ServerChunkCache) world_in.getChunkSource()).level;
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
                if (Impl.logged.add(type)) PokecubeCore.LOGGER.info("Lost Cities Structure: " + type);
                if (PokecubeTerrainChecker.structureSubbiomeMap.containsKey(type))
                    type = PokecubeTerrainChecker.structureSubbiomeMap.get(type);
                return BiomeType.getBiome(type, true);
            }
            return this.parent.getSubBiome(world_in, v, segment, caveAdjusted);
        }

    }
}