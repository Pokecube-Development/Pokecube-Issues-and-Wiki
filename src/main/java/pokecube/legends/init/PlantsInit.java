package pokecube.legends.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
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
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.blocks.plants.PlantBase;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class PlantsInit {
	
	public static List<Block> BLOCKFLOWERS = new ArrayList<>();
	public static final Block block = null;
	
    // Plants
    public static FlowerBlock MUSH_PLANT1       = new PlantBase("mush_plant1", Material.PLANTS, 0.5f, 5f, SoundType.PLANT);
    public static FlowerBlock MUSH_PLANT2       = new PlantBase("mush_plant2", Material.PLANTS, 0.5f, 5f, SoundType.PLANT);
    
    
    //Spawn Plants
    public void init(FMLCommonSetupEvent event) {
    	SpawnPlant(MUSH_PLANT1, "pokecube_legends:ub001", 3);
    	SpawnPlant(MUSH_PLANT2, "pokecube_legends:ub001", 2);
	}
    
    public void SpawnPlant(FlowerBlock block, String biomeName, int spawnRate) {
		FlowersFeature feature = new FlowersFeature(NoFeatureConfig::deserialize) {
			@Override
			public BlockState getRandomFlower(Random random, BlockPos pos) {
				return block.getDefaultState();
			}

			@Override
			public boolean place(IWorld world, ChunkGenerator<?> generator, Random random, BlockPos pos, NoFeatureConfig config) {
				DimensionType dimensionType = world.getDimension().getType();
				boolean dimensionCriteria = false;
				if (dimensionType == ModDimensions.DIMENSION_TYPE)
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
					Biome.createDecoratedFeature(feature, IFeatureConfig.NO_FEATURE_CONFIG, Placement.COUNT_HEIGHTMAP_32, new FrequencyConfig(spawnRate)));
		}
	}
}
