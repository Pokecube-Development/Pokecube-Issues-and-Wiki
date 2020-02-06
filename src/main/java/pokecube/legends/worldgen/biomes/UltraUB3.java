package pokecube.legends.worldgen.biomes;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.BlockInit;

public class UltraUB3 extends Biome
{
    // Pheromosa/Stakataka/Celestila
    public UltraUB3()
    {
        super(new Biome.Builder()
                .downfall(1f)
                .depth(0.3f)
                .scale(0.3f)
                .temperature(0.65f)
                .precipitation(Biome.RainType.NONE)
                .category(Biome.Category.DESERT)
                .waterColor(-9714980)
                .waterFogColor(-9714980)
                .surfaceBuilder(
                        SurfaceBuilder.DEFAULT,
                        new SurfaceBuilderConfig(BlockInit.ULTRA_SAND.getDefaultState(), BlockInit.ULTRA_SANDSTONE.getDefaultState(),
                                BlockInit.ULTRA_SANDSTONE.getDefaultState())));
        //setRegistryName("testar");
        DefaultBiomeFeatures.addCarvers(this);
        DefaultBiomeFeatures.addStructures(this);
        DefaultBiomeFeatures.addMonsterRooms(this);
        DefaultBiomeFeatures.addOres(this);

        //super(new Biome.Builder().precipitation(RainType.NONE).temperature(1.0f).scale(0.2f).depth(0.3f));
        // super(new
        // BiomeProperties("UB03").setRainDisabled().setBaseHeight(0.3f).setTemperature(1.0f).setHeightVariation(
        // 0.2f));

        // topBlock = BlockInit.ULTRA_SAND.getDefaultState();
        // fillerBlock = BlockInit.ULTRA_SANDSTONE.getDefaultState();
        // this.decorator.treesPerChunk = 0;
        // this.decorator.cactiPerChunk = 4;
        // this.decorator.deadBushPerChunk = 2;
        // this.decorator.clayPerChunk = 8;
        // this.spawnableCaveCreatureList.clear();
        // this.spawnableCreatureList.clear();
        // this.spawnableMonsterList.clear();
        // this.spawnableWaterCreatureList.clear();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(final double p_225528_1_, final double p_225528_3_)
    {
        return -1381717;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor()
    {
        return -1381717;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColor()
    {
        return -11272211;
    }
}
