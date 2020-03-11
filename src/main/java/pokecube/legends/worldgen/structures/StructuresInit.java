package pokecube.legends.worldgen.structures;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.world.gen.template.PokecubeStructureProcessor;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class StructuresInit
{

    public void init(final FMLCommonSetupEvent event)
    { // 10.000 Chunk - spawnRate

        // Biome UB01
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 900, "ub01_1", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 600, "ub01_2", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 500, "ub01_3", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 450, "ub01_4", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 400, "ub01_5", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 350, "ub01_6", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 300, "ub01_7", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 460, "ub01_8", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 250, "ub01_9", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 200, "ub01_10", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);

        // Biome UB02
        this.SpawnBuild(BlockInit.ULTRA_GRASSJUN, 810, "ub02_1", "pokecube_legends:ub002",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSJUN, 760, "ub02_2", "pokecube_legends:ub002",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSJUN, 400, "ub02_3", "pokecube_legends:ub002",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSJUN, 300, "ub02_4", "pokecube_legends:ub002",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSJUN, 300, "ub02_5", "pokecube_legends:ub002",
                GenerationStage.Decoration.SURFACE_STRUCTURES);

        // Biome UB03
        this.SpawnBuild(BlockInit.ULTRA_SAND, 70, "ub03_1", "pokecube_legends:ub003",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_SAND, 600, "ub03_2", "pokecube_legends:ub003",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_SAND, 700, "ub03_3", "pokecube_legends:ub003",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_SAND, 100, "ub03_4", "pokecube_legends:ub003",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_SAND, 100, "ub03_5", "pokecube_legends:ub003",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_SAND, 200, "ub03_6", "pokecube_legends:ub003",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_SAND, 150, "ub03_7", "pokecube_legends:ub003",
                GenerationStage.Decoration.SURFACE_STRUCTURES);

        // Biome UB04
        this.SpawnBuildFly(BlockInit.ULTRA_COBBLES, 300, "ub03_float", "pokecube_legends:ub004",
                GenerationStage.Decoration.RAW_GENERATION);
        this.SpawnBuild(BlockInit.ULTRA_COBBLES, 90, "ub04_1", "pokecube_legends:ub004",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_COBBLES, 150, "ub04_2", "pokecube_legends:ub004",
                GenerationStage.Decoration.SURFACE_STRUCTURES);

        // Extra
        this.SpawnBuild(BlockInit.ULTRA_GRASSMUSS, 330, "crystal_base", "pokecube_legends:ub001",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_GRASSJUN, 310, "crystal_base", "pokecube_legends:ub002",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(BlockInit.ULTRA_SAND, 320, "crystal_base", "pokecube_legends:ub003",
                GenerationStage.Decoration.SURFACE_STRUCTURES);
    }

    public void SpawnBuild(final Block blockPlace, final int spawnRate, final String nameBuild, final String nameBiome,
            final GenerationStage.Decoration deco)
    {
        final Feature<NoFeatureConfig> feature = new Feature<NoFeatureConfig>(NoFeatureConfig::deserialize)
        {
            @Override
            public boolean place(final IWorld iworld, final ChunkGenerator<?> generator, final Random random,
                    final BlockPos pos, final NoFeatureConfig config)
            {
                int i = pos.getX();
                int k = pos.getZ();
                final DimensionType dimensionType = iworld.getDimension().getType();
                boolean dimensionCriteria = false;
                if (dimensionType == ModDimensions.DIMENSION_TYPE) dimensionCriteria = true;
                if (!dimensionCriteria) return false;
                if (random.nextInt(10000) + 1 <= spawnRate)
                {
                    i += random.nextInt(16) + 8;
                    k += random.nextInt(16) + 8;
                    int j = iworld.getHeight(Heightmap.Type.WORLD_SURFACE, i, k);
                    j -= 1;
                    final BlockState blockAt = iworld.getBlockState(new BlockPos(i, j, k));
                    boolean blockCriteria = false;
                    if (blockAt.getBlock() == blockPlace.getDefaultState().getBlock()) blockCriteria = true;
                    if (!blockCriteria) return false;
                    final Template template = ((ServerWorld) iworld.getWorld()).getSaveHandler()
                            .getStructureTemplateManager().getTemplateDefaulted(new ResourceLocation(Reference.ID,
                                    nameBuild));
                    if (template == null) return false;
                    final Rotation rotation = Rotation.values()[random.nextInt(3)];
                    final Mirror mirror = Mirror.values()[random.nextInt(2)];
                    final BlockPos spawnTo = new BlockPos(i, j - 2, k);
                    template.addBlocksToWorldChunk(iworld, spawnTo, new PlacementSettings().addProcessor(
                            PokecubeStructureProcessor.PROCESSOR).setRotation(rotation).setRandom(random).setMirror(
                                    mirror).setChunk((ChunkPos) null).setIgnoreEntities(false));
                    return true;
                }
                return false;
            }
        };
        for (final Biome biome : ForgeRegistries.BIOMES.getValues())
        {
            boolean biomeCriteria = false;
            if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation(nameBiome))) biomeCriteria = true;
            if (!biomeCriteria) continue;
            biome.addFeature(deco, Biome.createDecoratedFeature(feature, IFeatureConfig.NO_FEATURE_CONFIG,
                    Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
        }

    }

    public void SpawnBuildFly(final Block blockPlace, final int spawnRate, final String nameBuild,
            final String nameBiome, final GenerationStage.Decoration deco)
    {
        final Feature<NoFeatureConfig> feature = new Feature<NoFeatureConfig>(NoFeatureConfig::deserialize)
        {
            @Override
            public boolean place(final IWorld iworld, final ChunkGenerator<?> generator, final Random random,
                    final BlockPos pos, final NoFeatureConfig config)
            {
                int i = pos.getX();
                int k = pos.getZ();
                final DimensionType dimensionType = iworld.getDimension().getType();
                boolean dimensionCriteria = false;
                if (dimensionType == ModDimensions.DIMENSION_TYPE) dimensionCriteria = true;
                if (!dimensionCriteria) return false;
                if (random.nextInt(10000) + 1 <= spawnRate)
                {
                    i += random.nextInt(16) + 8;
                    k += random.nextInt(16) + 8;
                    int j = iworld.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, i, k);
                    j += random.nextInt(50) + 16;
                    final BlockState blockAt = iworld.getBlockState(new BlockPos(i, j, k));
                    boolean blockCriteria = false;
                    if (blockAt.getBlock() == blockPlace.getDefaultState().getBlock()) blockCriteria = true;
                    if (!blockCriteria) return false;
                    final Template template = ((ServerWorld) iworld.getWorld()).getSaveHandler()
                            .getStructureTemplateManager().getTemplateDefaulted(new ResourceLocation(Reference.ID,
                                    nameBuild));
                    if (template == null) return false;
                    final Rotation rotation = Rotation.values()[random.nextInt(3)];
                    final Mirror mirror = Mirror.values()[random.nextInt(2)];
                    final BlockPos spawnTo = new BlockPos(i, j + 30, k);
                    template.addBlocksToWorldChunk(iworld, spawnTo, new PlacementSettings().addProcessor(
                            PokecubeStructureProcessor.PROCESSOR).setRotation(rotation).setRandom(random).setMirror(
                                    mirror).setChunk((ChunkPos) null).setIgnoreEntities(false));
                    return true;
                }
                return false;
            }
        };
        for (final Biome biome : ForgeRegistries.BIOMES.getValues())
        {
            boolean biomeCriteria = false;
            if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation(nameBiome))) biomeCriteria = true;
            if (!biomeCriteria) continue;
            biome.addFeature(deco, Biome.createDecoratedFeature(feature, IFeatureConfig.NO_FEATURE_CONFIG,
                    Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
        }

    }
}
