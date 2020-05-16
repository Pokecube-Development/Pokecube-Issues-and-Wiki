package pokecube.legends.worldgen.biomes;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
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
        // super(new
        // Biome.Builder().precipitation(RainType.NONE).temperature(0.8f));
        super(new Biome.Builder().downfall(0f).depth(0.3f).scale(0.3f).temperature(0.3f)
                .precipitation(Biome.RainType.NONE).category(Biome.Category.MUSHROOM).waterColor(-1).waterFogColor(-1)
                .surfaceBuilder(SurfaceBuilder.DEFAULT,
                        new SurfaceBuilderConfig(BlockInit.ULTRA_GRASSMUSS.getDefaultState(),
                                BlockInit.ULTRA_DIRTMUSS.getDefaultState(),
                                BlockInit.ULTRA_DIRTMUSS.getDefaultState())));
        // setRegistryName("testar");
        DefaultBiomeFeatures.addCarvers(this);
        DefaultBiomeFeatures.addStructures(this);
        DefaultBiomeFeatures.addMonsterRooms(this);
        DefaultBiomeFeatures.addOres(this);

        this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
                Feature.RANDOM_PATCH.withConfiguration(DefaultBiomeFeatures.GRASS_CONFIG)
                .withPlacement(Placement.COUNT_HEIGHTMAP_DOUBLE.configure(new FrequencyConfig(3))));
        this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
                Feature.RANDOM_PATCH.withConfiguration(DefaultBiomeFeatures.BROWN_MUSHROOM_CONFIG)
                .withPlacement(Placement.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceConfig(4))));
        this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
                Feature.RANDOM_PATCH.withConfiguration(DefaultBiomeFeatures.RED_MUSHROOM_CONFIG)
                .withPlacement(Placement.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceConfig(4))));

        this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
                Feature.RANDOM_BOOLEAN_SELECTOR
                .withConfiguration(new TwoFeatureChoiceConfig(
                        Feature.HUGE_RED_MUSHROOM.withConfiguration(DefaultBiomeFeatures.BIG_RED_MUSHROOM),
                        Feature.HUGE_BROWN_MUSHROOM.withConfiguration(DefaultBiomeFeatures.BIG_BROWN_MUSHROOM)))
                .withPlacement(Placement.COUNT_HEIGHTMAP.configure(new FrequencyConfig(3))));
        // }

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

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getGrassColor(final double p_225528_1_, final double p_225528_3_)
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
    }
}
