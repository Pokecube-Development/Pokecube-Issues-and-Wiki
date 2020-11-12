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

public class UltraUB4 {
	
    /*
	public static class customFillerBlockType
    {
        public static final OreFeatureConfig.FillerBlockType CUSTOM_FILLER = OreFeatureConfig.FillerBlockType.create("CustomFiller", "custom_filler", new BlockMatcher(BlockInit.ULTRA_STONE.get()));
    }
	
    // Guzzlord
    public UltraUB4()
    {     
        super(new Biome.Builder().downfall(1f).depth(0.3f).scale(0.2f).temperature(2f)
        		.precipitation(Biome.RainType.RAIN).category(Biome.Category.DESERT).waterColor(-6880509)
        		.waterFogColor(-6880509).surfaceBuilder(SurfaceBuilder.DEFAULT,
        				new SurfaceBuilderConfig(BlockInit.ULTRA_DARKSTONE.get().getDefaultState(),
        						BlockInit.ULTRA_DARKSTONE.get().getDefaultState(),
        						Blocks.MOSSY_COBBLESTONE.getDefaultState())));
		
		DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DefaultBiomeFeatures.addOres(this);
        DefaultBiomeFeatures.addFossils(this);
        		
      //Extra
        this.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE.withConfiguration(
                new OreFeatureConfig(customFillerBlockType.CUSTOM_FILLER, 
                		BlockInit.SPECTRUM_ORE.get().getDefaultState(), 8))
        					.withPlacement(Placement.COUNT_RANGE.configure(
                                new CountRangeConfig(10, 0, 0, 32))));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(final double posX, final double posZ)
    {
        return -14932710;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor()
    {
        return -14932710;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColor()
    {
        return -16447456;
    }*/
}
