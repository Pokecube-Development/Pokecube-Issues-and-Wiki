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
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class StructuresInit
{

    public void init(final FMLCommonSetupEvent event)
    { // 10.000 Chunk - spawnRate
      // Biome UB01
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 260, "ub1_build1",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 210, "ub1_build2",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 800, "ub1_build3",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 700, "ub1_build4",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 600, "ub1_build5",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 500, "ub1_build6",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 400, "ub1_build7",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 300, "ub1_build8",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 200, "ub1_build9",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 220, "ub1_build10",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);

        // extra
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 270, "ub1_build9",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSMUSS, 260, "ub1_build9",
                "pokecube_legends:ub001", GenerationStage.Decoration.SURFACE_STRUCTURES);

        // Biome UB03
        this.SpawnBuild(Heightmap.Type.WORLD_SURFACE, BlockInit.ULTRA_GRASSJUN, 900, "ub2_build1",
                "pokecube_legends:ub002", GenerationStage.Decoration.SURFACE_STRUCTURES);
    }

    public void SpawnBuild(final Type worldSurface, final Block blockPlace, final int spawnRate, final String nameBuild,
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
                    int j = iworld.getHeight(worldSurface, i, k);
                    j -= 1;
                    final BlockState blockAt = iworld.getBlockState(new BlockPos(i, j, k));
                    boolean blockCriteria = false;
                    if (blockAt.getBlock() == blockPlace.getDefaultState().getBlock()) blockCriteria = true;
                    if (!blockCriteria) return false;
                    final Template template = ((ServerWorld) iworld.getWorld()).getSaveHandler()
                            .getStructureTemplateManager()
                            .getTemplateDefaulted(new ResourceLocation(Reference.ID, nameBuild));
                    if (template == null) return false;
                    final Rotation rotation = Rotation.values()[random.nextInt(3)];
                    final Mirror mirror = Mirror.values()[random.nextInt(2)];
                    final BlockPos spawnTo = new BlockPos(i, j + 0, k);
                    template.addBlocksToWorldChunk(iworld, spawnTo, new PlacementSettings().setRotation(rotation)
                            .setRandom(random).setMirror(mirror).setChunk((ChunkPos) null).setIgnoreEntities(false));
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
            biome.addFeature(deco, feature.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
        }

    }
}
