package pokecube.legends.worldgen.biomes;

import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.blocks.plants.Ultra_Tree03;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.biomes.UltraUB1.customFillerBlockType;

public class UltraUB6 extends Biome
{
	public static class customFillerBlockType
    {
        public static final OreFeatureConfig.FillerBlockType CUSTOM_FILLER = OreFeatureConfig.FillerBlockType.create("CustomFiller", "custom_filler", new BlockMatcher(BlockInit.ULTRA_STONE.get()));
    }
	
    // Kartana
    public UltraUB6()
    {     
        super(new Biome.Builder().downfall(0.3f).depth(0.5f).scale(0.1f).temperature(1.4f)
        		.precipitation(Biome.RainType.RAIN).category(Biome.Category.OCEAN).waterColor(-3342388)
        		.waterFogColor(-3342388).surfaceBuilder(SurfaceBuilder.DEFAULT,
        				new SurfaceBuilderConfig(BlockInit.ULTRA_GRASSAGED.get().getDefaultState(),
        						BlockInit.ULTRA_DIRTAGED.get().getDefaultState(),
        						Blocks.MOSSY_COBBLESTONE.getDefaultState())));
		
		DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addBamboo(this);
		DefaultBiomeFeatures.addOres(this);
        
		this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
                Feature.RANDOM_PATCH.withConfiguration(DefaultBiomeFeatures.GRASS_CONFIG)
                .withPlacement(Placement.COUNT_HEIGHTMAP_DOUBLE.configure(new FrequencyConfig(4))));
        
		//Extra
        	this.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE.withConfiguration(
                new OreFeatureConfig(customFillerBlockType.CUSTOM_FILLER, 
                		BlockInit.SPECTRUM_ORE.get().getDefaultState(), 8))
        					.withPlacement(Placement.COUNT_RANGE.configure(
                                new CountRangeConfig(10, 0, 0, 32))));
        	this.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE.withConfiguration(
                    new OreFeatureConfig(customFillerBlockType.CUSTOM_FILLER, 
                    		BlockInit.COSMIC_DUST_ORE.get().getDefaultState(), 8))
            					.withPlacement(Placement.COUNT_RANGE.configure(
                                    new CountRangeConfig(10, 0, 0, 32))));
        
      		this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Feature.NORMAL_TREE.withConfiguration(Ultra_Tree03.ULTRA_TREE03_CONFIG)
      				.withPlacement(Placement.COUNT_EXTRA_HEIGHTMAP.configure(new AtSurfaceWithExtraConfig(2, 0.1f, 1))));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(final double posX, final double posZ)
    {
        return -256;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor()
    {
        return -256;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColor()
    {
        return -39424;
    }
}
