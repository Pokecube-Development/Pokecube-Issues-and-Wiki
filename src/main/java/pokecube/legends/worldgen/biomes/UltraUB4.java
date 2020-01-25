package pokecube.legends.worldgen.biomes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UltraUB4 extends Biome
{
    // Guzzlord
    public UltraUB4()
    {
        super(new Biome.Builder().precipitation(RainType.RAIN).downfall(1.0f).temperature(0.1f).scale(0.3f).depth(
                0.3f));
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
    public int getGrassColor(final BlockPos pos)
    {
        return -12959190;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor(final BlockPos pos)
    {
        return -12959190;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColorByTemp(final float currentTemperature)
    {
        return -16777114;
    }
}
