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
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.DefaultFlowersFeature;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.PlantBase;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class PlantsInit
{

    // Plants
    public static RegistryObject<Block> MUSH_PLANT1;
    public static RegistryObject<Block> MUSH_PLANT2;
    public static RegistryObject<Block> AGED_FLOWER;
    public static RegistryObject<Block> DIRST_FLOWER;

    static
    {
        MUSH_PLANT1 = PokecubeLegends.BLOCKS_TAB.register("mush_plant1", () -> new PlantBase(Material.PLANTS,
                0f, 3f, SoundType.PLANT));
        MUSH_PLANT2 = PokecubeLegends.BLOCKS_TAB.register("mush_plant2", () -> new PlantBase(Material.PLANTS,
                0f, 3f, SoundType.PLANT));
        AGED_FLOWER = PokecubeLegends.BLOCKS_TAB.register("a1_flower", () -> new PlantBase(Material.PLANTS,
                0f, 3f, SoundType.CORAL));
        DIRST_FLOWER = PokecubeLegends.BLOCKS_TAB.register("b1_flower", () -> new PlantBase(Material.PLANTS,
                0f, 3f, SoundType.BAMBOO_SAPLING));
    }

    public static void registry() {
    	
    }
    
    public void init(final FMLCommonSetupEvent event) 
    {
        SpawnPlant(MUSH_PLANT1.get(), "pokecube_legends:ub001", 2);
        SpawnPlant(MUSH_PLANT2.get(), "pokecube_legends:ub001", 2);
        SpawnPlant(AGED_FLOWER.get(), "pokecube_legends:ub006", 2);
        SpawnPlant(DIRST_FLOWER.get(), "pokecube_legends:ub005", 1);
    }

	public static void SpawnPlant(final Block block, final String biomeName, final int spawnRate)
    {
    	FlowersFeature<BlockClusterFeatureConfig> feature = new DefaultFlowersFeature(BlockClusterFeatureConfig::deserialize) {
			@Override
			public BlockState getFlowerToPlace(Random random, BlockPos bp, BlockClusterFeatureConfig fc) {
				return block.getDefaultState();
			}

			@Override
			public boolean place(IWorld world, ChunkGenerator<?> generator, Random random, BlockPos pos, BlockClusterFeatureConfig config) {
				DimensionType dimensionType = world.getDimension().getType();
				boolean dimensionCriteria = false;
				if (dimensionType == ModDimensions.DIMENSION_TYPE_US)
					dimensionCriteria = true;
				if (!dimensionCriteria)
					return false;
				return super.place(world, generator, random, pos, config);
			}
		};
		for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
			boolean biomeCriteria = false;
			if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation(biomeName)))
				biomeCriteria = true;
			if (!biomeCriteria)
				continue;
			biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
					feature.withConfiguration(
							(new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(block.getDefaultState()), new SimpleBlockPlacer()))
									.tries(64).build())
							.withPlacement(Placement.COUNT_HEIGHTMAP_32.configure(new FrequencyConfig(spawnRate))));
		}
    }
}
