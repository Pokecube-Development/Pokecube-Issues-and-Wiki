package pokecube.legends.worldgen.biomes;

import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.BlockInit;

public class UltraUB5 {
/*
	public static class customFillerBlockType
    {
        public static final OreFeatureConfig.FillerBlockType CUSTOM_FILLER = OreFeatureConfig.FillerBlockType.create("CustomFiller", "custom_filler", new BlockMatcher(BlockInit.ULTRA_STONE.get()));
    }

    //
    public UltraUB5()
    {
        super(new Biome.Builder().downfall(0.5f).depth(0.6f).scale(0.1f).temperature(1.5f)
        		.precipitation(Biome.RainType.RAIN).category(Biome.Category.THEEND).waterColor(-8650599)
        		.waterFogColor(-8650599).surfaceBuilder(SurfaceBuilder.DEFAULT,
        				new SurfaceBuilderConfig(BlockInit.ULTRA_SANDDISTOR.get().getDefaultState(),
        						BlockInit.ULTRA_ROCKDISTOR.get().getDefaultState(),
        						Blocks.MOSSY_COBBLESTONE.getDefaultState())));

		DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DefaultBiomeFeatures.addOres(this);

		this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
                Feature.RANDOM_PATCH.withConfiguration(DefaultBiomeFeatures.GRASS_CONFIG)
                .withPlacement(Placement.COUNT_HEIGHTMAP_DOUBLE.configure(new FrequencyConfig(3))));

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
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(final double posX, final double posZ)
    {
        return -13540445;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor()
    {
        return -13540445;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColor()
    {
        return -2309035;
    }*/
}
