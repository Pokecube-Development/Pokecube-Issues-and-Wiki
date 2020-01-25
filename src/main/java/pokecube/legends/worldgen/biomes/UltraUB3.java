package pokecube.legends.worldgen.biomes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UltraUB3 extends Biome
{
    // Pheromosa/Stakataka/Celestila
    public UltraUB3()
    {
        super(new Biome.Builder().precipitation(RainType.NONE).temperature(1.0f).scale(0.2f).depth(0.3f));
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
    public int getGrassColor(final BlockPos pos)
    {
        return -10511132;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor(final BlockPos pos)
    {
        return -10511132;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColorByTemp(final float currentTemperature)
    {
        return -6697729;
    }
}
