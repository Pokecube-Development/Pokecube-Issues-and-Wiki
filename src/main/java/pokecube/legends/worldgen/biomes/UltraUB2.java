package pokecube.legends.worldgen.biomes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UltraUB2 extends Biome
{
    // Buzzwole/Poipole/Kartana
    public UltraUB2()
    {
        super(new Biome.Builder().precipitation(RainType.RAIN).downfall(1.0f).temperature(0.8f).scale(0.5f));
        // super(new
        // BiomeProperties("UB02").setRainfall(1.0f).setBaseHeight(0.2f).setTemperature(0.8f).setHeightVariation(
        // 0.5f));
        // topBlock = BlockInit.ULTRA_GRASSJUN.getDefaultState();
        // fillerBlock = BlockInit.ULTRA_DIRTJUN.getDefaultState();
        // this.decorator.treesPerChunk = 20;
        // this.decorator.flowersPerChunk = 10;
        // this.decorator.grassPerChunk = 24;
        // this.spawnableCaveCreatureList.clear();
        // this.spawnableCreatureList.clear();
        // this.spawnableMonsterList.clear();
        // this.spawnableWaterCreatureList.clear();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor(final BlockPos pos)
    {
        return -16737997;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColorByTemp(final float currentTemperature)
    {
        return -16750900;
    }
}
