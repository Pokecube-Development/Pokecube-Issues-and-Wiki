package pokecube.legends.worldgen.biomes;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import net.minecraft.world.gen.feature.BushConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.GrassFeatureConfig;
import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.BlockInit;

public class UltraUB1 extends Biome
{
    // Nhihilego/Xurkitree/Blacephalun
    public UltraUB1()
    {
        //super(new Biome.Builder().precipitation(RainType.NONE).temperature(0.8f));
        super(new Biome.Builder()
        		.downfall(0f)
				.depth(0.3f)
				.scale(0.3f)
				.temperature(0.3f)
				.precipitation(Biome.RainType.NONE)
				.category(Biome.Category.MUSHROOM)
				.waterColor(-1)
				.waterFogColor(-1)
				.surfaceBuilder(
						SurfaceBuilder.DEFAULT,
						new SurfaceBuilderConfig(BlockInit.ULTRA_GRASSMUSS.getDefaultState(), BlockInit.ULTRA_DIRTMUSS.getDefaultState(),
								BlockInit.ULTRA_DIRTMUSS.getDefaultState())));
		//setRegistryName("testar");
		DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DefaultBiomeFeatures.addOres(this);
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.GRASS,
				new GrassFeatureConfig(Blocks.GRASS.getDefaultState()), Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(3)));
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.BUSH,
				new BushConfig(Blocks.BROWN_MUSHROOM.getDefaultState()), Placement.CHANCE_HEIGHTMAP_DOUBLE, new ChanceConfig(4)));
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.BUSH,
				new BushConfig(Blocks.RED_MUSHROOM.getDefaultState()), Placement.CHANCE_HEIGHTMAP_DOUBLE, new ChanceConfig(4)));
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.RANDOM_BOOLEAN_SELECTOR,
				new TwoFeatureChoiceConfig(Feature.HUGE_RED_MUSHROOM, new BigMushroomFeatureConfig(false), Feature.HUGE_BROWN_MUSHROOM,
						new BigMushroomFeatureConfig(false)), Placement.COUNT_HEIGHTMAP, new FrequencyConfig(3)));
	//}


        // topBlock = BlockInit.ULTRA_GRASSMUSS.getDefaultState();
        // fillerBlock = BlockInit.ULTRA_DIRTMUSS.getDefaultState();
        // this.decorator.treesPerChunk = 0;
        // this.decorator.grassPerChunk = 2;
        // this.decorator.flowersPerChunk = 0;
        // this.decorator.mushroomsPerChunk = 2;
        // this.decorator.bigMushroomsPerChunk = 5;
        // this.spawnableCaveCreatureList.clear();
        // this.spawnableCreatureList.clear();
        // this.spawnableMonsterList.clear();
        // this.spawnableWaterCreatureList.clear();
    }

    @OnlyIn(Dist.CLIENT)
	@Override
	public int getGrassColor(BlockPos pos) {
		return -13491147;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getFoliageColor(BlockPos pos) {
		return -13491147;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getSkyColorByTemp(float currentTemperature) {
		return -14339742;
	}
}
