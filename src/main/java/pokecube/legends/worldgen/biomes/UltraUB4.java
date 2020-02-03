package pokecube.legends.worldgen.biomes;

import com.google.common.collect.Lists;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.BlockInit;

public class UltraUB4 extends Biome
{
    // Guzzlord
    public UltraUB4()
    {
    	super(new Biome.Builder()
    			.downfall(1f)
				.depth(0.3f)
				.scale(0.3f)
				.temperature(0.65f)
				.precipitation(Biome.RainType.SNOW)
				.category(Biome.Category.ICY)
				.waterColor(-6880509)
				.waterFogColor(-6880509)
				.surfaceBuilder(
						SurfaceBuilder.DEFAULT,
						new SurfaceBuilderConfig(BlockInit.ULTRA_COBBLES.getDefaultState(), BlockInit.ULTRA_STONE.getDefaultState(),
								BlockInit.ULTRA_STONE.getDefaultState())));
		//setRegistryName("testar");
		DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addOres(this);
		DefaultBiomeFeatures.addLakes(this);
		DefaultBiomeFeatures.addFossils(this);
		addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(
				Feature.DISK,
				new SphereReplaceConfig(Blocks.GRAVEL.getDefaultState(), 6, 2, Lists.newArrayList(Blocks.DIRT.getDefaultState(),
						Blocks.GRASS_BLOCK.getDefaultState())), Placement.COUNT_TOP_SOLID, new FrequencyConfig(15)));
        //super(new Biome.Builder().precipitation(RainType.RAIN).downfall(1.0f).temperature(0.1f).scale(0.3f).depth(
        //        0.3f));
        // super(new
        // BiomeProperties("UB04").setBaseHeight(0.3f).setRainfall(1.0F).setTemperature(0.1f).setHeightVariation(
        // 0.3f));

        // topBlock = BlockInit.ULTRA_COBBLES.getDefaultState();
        // fillerBlock = BlockInit.ULTRA_STONE.getDefaultState();
        // this.decorator.treesPerChunk = 0;
        // this.decorator.flowersPerChunk = 0;
        // this.decorator.mushroomsPerChunk = 0;
        // this.spawnableCaveCreatureList.clear();
        // this.spawnableCreatureList.clear();
        // this.spawnableMonsterList.clear();
        // this.spawnableWaterCreatureList.clear();
        // this.decorator.generateFalls = true;
    }

    @OnlyIn(Dist.CLIENT)
	@Override
	public int getGrassColor(BlockPos pos) {
		return -14932710;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getFoliageColor(BlockPos pos) {
		return -14932710;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getSkyColorByTemp(float currentTemperature) {
		return -16447456;
	}
}
