package pokecube.legends.worldgen.biomes;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.BlockInit;

public class DistorticBiome extends Biome
{	
    //Giratinaa
    public DistorticBiome()
    {       
        super(new Biome.Builder().downfall(0.8f).depth(0.1f).scale(0.2f).temperature(1.5f)
        		.precipitation(Biome.RainType.NONE).category(Biome.Category.NONE).waterColor(-13012799).waterFogColor(-13012799)
				.surfaceBuilder(SurfaceBuilder.DEFAULT,
						new SurfaceBuilderConfig(BlockInit.DISTORTIC_GRASS.get().getDefaultState(),
								BlockInit.DISTORTIC_STONE.get().getDefaultState(), 
								BlockInit.DISTORTIC_STONE.get().getDefaultState())));

        DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DistorticBiomeFeautures.addLakes(this);

		this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Feature.RANDOM_PATCH.withConfiguration(DefaultBiomeFeatures.GRASS_CONFIG)
				.withPlacement(Placement.COUNT_HEIGHTMAP_DOUBLE.configure(new FrequencyConfig(1))));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getGrassColor(final double posX, final double posZ)
    {
        return -13489378;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor()
    {
        return -13489378;
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColor()
    {
        return -3407668;
    }
}
