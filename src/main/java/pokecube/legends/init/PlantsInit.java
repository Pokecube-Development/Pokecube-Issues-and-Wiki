package pokecube.legends.init;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.PlantBase;

public class PlantsInit
{

    // Plants
    public static RegistryObject<Block> MUSH_PLANT1;
    public static RegistryObject<Block> MUSH_PLANT2;
    public static RegistryObject<Block> AGED_FLOWER;
    public static RegistryObject<Block> DIRST_FLOWER;

    static
    {
        PlantsInit.MUSH_PLANT1 = PokecubeLegends.BLOCKS_TAB.register("mush_plant1", () -> new PlantBase(Material.PLANTS,
                0f, 3f, SoundType.PLANT));
        PlantsInit.MUSH_PLANT2 = PokecubeLegends.BLOCKS_TAB.register("mush_plant2", () -> new PlantBase(Material.PLANTS,
                0f, 3f, SoundType.PLANT));
        PlantsInit.AGED_FLOWER = PokecubeLegends.BLOCKS_TAB.register("a1_flower", () -> new PlantBase(Material.PLANTS,
                0f, 3f, SoundType.CORAL));
        PlantsInit.DIRST_FLOWER = PokecubeLegends.BLOCKS_TAB.register("b1_flower", () -> new PlantBase(Material.PLANTS,
                0f, 3f, SoundType.BAMBOO_SAPLING));
    }

    public static void registry() {

    }

    /*public void init(final FMLCommonSetupEvent event)
    {
        PlantsInit.SpawnPlant(PlantsInit.MUSH_PLANT1.get(), "pokecube_legends:ub001", 2);
        PlantsInit.SpawnPlant(PlantsInit.MUSH_PLANT2.get(), "pokecube_legends:ub001", 2);
        PlantsInit.SpawnPlant(PlantsInit.AGED_FLOWER.get(), "pokecube_legends:ub006", 2);
        PlantsInit.SpawnPlant(PlantsInit.DIRST_FLOWER.get(), "pokecube_legends:ub005", 1);
    }*/

	/*public static void SpawnPlant(final Block block, final String biomeName, final int spawnRate)
    {
    	final FlowersFeature<BlockClusterFeatureConfig> feature = new DefaultFlowersFeature(BlockClusterFeatureConfig::deserialize) {
			@Override
			public BlockState getFlowerToPlace(final Random random, final BlockPos bp, final BlockClusterFeatureConfig fc) {
				return block.getDefaultState();
			}

			@Override
			public boolean place(final IWorld world, ChunkGenerator<?> generator, final Random random, final BlockPos pos, final BlockClusterFeatureConfig config) {
				final RegistryKey<World> dimensionType = world.getDimensionKey();
				boolean dimensionCriteria = false;
				if (dimensionType == ModDimensions.DIMENSION_TYPE_US)
					dimensionCriteria = true;
				if (!dimensionCriteria)
					return false;
				return super.place(world, generator, random, pos, config);
			}
		};
		for (final Biome biome : ForgeRegistries.BIOMES.getValues()) {
			boolean biomeCriteria = false;
			if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation(biomeName)))
				biomeCriteria = true;
			if (!biomeCriteria)
				continue;
			biome.ad(GenerationStage.Decoration.VEGETAL_DECORATION,
					feature.withConfiguration(
							new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(block.getDefaultState()), new SimpleBlockPlacer())
									.tries(64).build())
							.withPlacement(Placement.HEIGHTMAP_WORLD_SURFACE.configure(new NoPlacementConfig())));
		}
    }*/
}
