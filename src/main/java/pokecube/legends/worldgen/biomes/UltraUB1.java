package pokecube.legends.worldgen.biomes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UltraUB1 extends Biome
{
    // Nhihilego/Xurkitree/Blacephalun
    public UltraUB1()
    {
        super(new Biome.Builder().precipitation(RainType.NONE).temperature(0.8f));

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

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(final BlockPos pos)
    {
        return -11394970;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor(final BlockPos pos)
    {
        return -11394970;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColorByTemp(final float currentTemperature)
    {
        return -14460312;
    }
}
