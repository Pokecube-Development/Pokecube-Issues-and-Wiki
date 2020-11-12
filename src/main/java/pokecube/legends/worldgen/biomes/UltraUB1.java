package pokecube.legends.worldgen.biomes;

import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.layer.BiomeLayer;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.blocks.plants.Ultra_Tree01;
import pokecube.legends.init.BlockInit;

public class UltraUB1 {
	/*public static class customFillerBlockType
    {
        public static final OreFeatureConfig.FillerBlockType CUSTOM_FILLER = 
        		OreFeatureConfig.FillerBlockType.BASE_STONE_NETHER;
    }
	
    // Nhihilego/Xurkitree/Blacephalun
    public UltraUB1()
    {       
        super(new Biome.Builder().downfall(0.8f).depth(0.1f).scale(0.2f).temperature(1.5f)
        		.precipitation(Biome.RainType.RAIN).category(Biome.Category.PLAINS));

        DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DefaultBiomeFeatures.addOres(this);
		DefaultBiomeFeatures.addLakes(this);

        this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
                Feature.RANDOM_PATCH.withConfiguration(DefaultBiomeFeatures.GRASS_CONFIG)
                .withPlacement(Placement.COUNT_HEIGHTMAP_DOUBLE.configure(new FrequencyConfig(3))));

		this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Feature.RANDOM_PATCH.withConfiguration(DefaultBiomeFeatures.GRASS_CONFIG)
				.withPlacement(Placement.COUNT_HEIGHTMAP_DOUBLE.configure(new FrequencyConfig(3))));
		
		//Extra
        this.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE.withConfiguration(
                new OreFeatureConfig(customFillerBlockType.CUSTOM_FILLER, 
                		BlockInit.SPECTRUM_ORE.get().getDefaultState(), 8))
        					.withPlacement(Placement.COUNT_RANGE.configure(
                                new CountRangeConfig(10, 0, 0, 32))));
        
		this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Feature.NORMAL_TREE.withConfiguration(Ultra_Tree01.ULTRA_TREE01_CONFIG)
				.withPlacement(Placement.COUNT_EXTRA_HEIGHTMAP.configure(new AtSurfaceWithExtraConfig(3, 0.1f, 1))));

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getGrassColor(final double posX, final double posZ)
    {
        return -13491147;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor()
    {
        return -13491147;
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColor()
    {
        return -14339742;
    }*/
}
