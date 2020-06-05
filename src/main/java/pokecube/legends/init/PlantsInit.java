package pokecube.legends.init;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.plants.PlantBase;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class PlantsInit
{

    // Plants
    public static RegistryObject<Block> MUSH_PLANT1;
    public static RegistryObject<Block> MUSH_PLANT2;

    static
    {
        PlantsInit.MUSH_PLANT1 = PokecubeLegends.BLOCKS.register("mush_plant1", () -> new PlantBase(Material.PLANTS,
                0.5f, 5f, SoundType.PLANT));
        PlantsInit.MUSH_PLANT2 = PokecubeLegends.BLOCKS.register("mush_plant2", () -> new PlantBase(Material.PLANTS,
                0.5f, 5f, SoundType.PLANT));
    }

    public static void init()
    {

    }

    // Spawn Plants
    public void init(final FMLCommonSetupEvent event)
    {
        this.SpawnPlant(PlantsInit.MUSH_PLANT1.get(), "pokecube_legends:ub001", 3);
        this.SpawnPlant(PlantsInit.MUSH_PLANT2.get(), "pokecube_legends:ub001", 2);
    }

    public void SpawnPlant(final Block block, final String biomeName, final int spawnRate)
    {
        final FlowersFeature feature = new FlowersFeature(NoFeatureConfig::deserialize)
        {
            @Override
            public BlockState getRandomFlower(final Random random, final BlockPos pos)
            {
                return block.getDefaultState();
            }

            @Override
            public boolean place(final IWorld world, final ChunkGenerator<?> generator, final Random random,
                    final BlockPos pos, final NoFeatureConfig config)
            {
                final DimensionType dimensionType = world.getDimension().getType();
                boolean dimensionCriteria = false;
                if (dimensionType == ModDimensions.DIMENSION_TYPE) dimensionCriteria = true;
                if (!dimensionCriteria) return false;
                return super.place(world, generator, random, pos, config);
            }
        };
        for (final Biome biome : ForgeRegistries.BIOMES.getValues())
        {
            boolean biomeCriteria = false;
            if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation(biomeName))) biomeCriteria = true;
            if (!biomeCriteria) continue;
            biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(feature,
                    IFeatureConfig.NO_FEATURE_CONFIG, Placement.COUNT_HEIGHTMAP_32, new FrequencyConfig(spawnRate)));
        }
    }
}
