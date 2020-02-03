package pokecube.legends.worldgen.biomes;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.GrassFeatureConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.MultipleRandomFeatureConfig;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.BlockInit;

public class UltraUB2 extends Biome
{
    // Buzzwole/Poipole/Kartana
    public UltraUB2()
    {
    	super(new Biome.Builder()
    			.downfall(0.5f)
				.depth(0.2f)
				.scale(0.4f)
				.temperature(0.5f)
				.precipitation(Biome.RainType.RAIN)
				.category(Biome.Category.JUNGLE)
				.waterColor(-10414997)
				.waterFogColor(-10414997)
				.surfaceBuilder(
						SurfaceBuilder.DEFAULT,
						new SurfaceBuilderConfig(BlockInit.ULTRA_GRASSJUN.getDefaultState(), BlockInit.ULTRA_DIRTJUN.getDefaultState(),
								BlockInit.ULTRA_DIRTJUN.getDefaultState())));
		//setRegistryName("testar");
		DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DefaultBiomeFeatures.addOres(this);
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.DEFAULT_FLOWER,
				IFeatureConfig.NO_FEATURE_CONFIG, Placement.COUNT_HEIGHTMAP_32, new FrequencyConfig(4)));
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.GRASS,
				new GrassFeatureConfig(Blocks.GRASS.getDefaultState()), Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(4)));
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.RANDOM_SELECTOR,
				new MultipleRandomFeatureConfig(new Feature[]{Feature.DARK_OAK_TREE}, new IFeatureConfig[]{IFeatureConfig.NO_FEATURE_CONFIG,
						IFeatureConfig.NO_FEATURE_CONFIG}, new float[]{1f / 3, 1f / 3}, Feature.NORMAL_TREE, IFeatureConfig.NO_FEATURE_CONFIG),
				Placement.COUNT_EXTRA_HEIGHTMAP, new AtSurfaceWithExtraConfig(20, 0.1F, 1)));

        //super(new Biome.Builder().precipitation(RainType.RAIN).downfall(1.0f).temperature(0.8f).scale(0.5f));
        // super(new
        // BiomeProperties("UB02").setRainfall(1.0f).setBaseHeight(0.2f).setTemperature(0.8f).setHeightVariation(
        // 0.5f));
        // topBlock = BlockInit.ULTRA_GRASSJUN.getDefaultState();
        // fillerBlock = BlockInit.ULTRA_DIRTJUN.getDefaultState();
        // this.decorator.treesPerChunk = 20;
        // this.decorator.flowersPerChunk = 10;
        // this.decorator.grassPerChunk = 24;
        // this.spawnableCaveCreatureList.clear();
        // this.spawnableCreatureList.clear();
        // this.spawnableMonsterList.clear();
        // this.spawnableWaterCreatureList.clear();
    }

    @OnlyIn(Dist.CLIENT)
	@Override
	public int getGrassColor(BlockPos pos) {
		return -12779667;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getFoliageColor(BlockPos pos) {
		return -12779667;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getSkyColorByTemp(float currentTemperature) {
		return -1704908;
	}
}
