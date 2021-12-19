package pokecube.legends.worldgen;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class UltraSpaceSurfaceSystem extends SurfaceSystem
{
    private static final BlockState LIGHT_BLUE_TERRACOTTA = Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
    private static final BlockState PURPLE_TERRACOTTA = Blocks.PURPLE_TERRACOTTA.defaultBlockState();
    private static final BlockState BLUE_TERRACOTTA = Blocks.BLUE_TERRACOTTA.defaultBlockState();
    private static final BlockState MAGENTA_TERRACOTTA = Blocks.MAGENTA_TERRACOTTA.defaultBlockState();
    private static final BlockState PINK_TERRACOTTA = Blocks.PINK_TERRACOTTA.defaultBlockState();
    private final BlockState defaultBlock;
    private final int seaLevel;
    private final BlockState[] clayBands;
    private final NormalNoise clayBandsOffsetNoise;
    private final NormalNoise badlandsPillarNoise;
    private final NormalNoise badlandsPillarRoofNoise;
    private final NormalNoise badlandsSurfaceNoise;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, PositionalRandomFactory> positionalRandoms = new ConcurrentHashMap<>();
    private final PositionalRandomFactory randomFactory;
    private final NormalNoise surfaceNoise;
    private final NormalNoise surfaceSecondaryNoise;

    public UltraSpaceSurfaceSystem(Registry<NormalNoise.NoiseParameters> p_198285_, BlockState p_198286_, int p_198287_, long p_198288_, WorldgenRandom.Algorithm p_198289_)
    {
        super(p_198285_, p_198286_, p_198287_, p_198288_, p_198289_);
        this.noises = p_198285_;
        this.defaultBlock = p_198286_;
        this.seaLevel = p_198287_;
        this.randomFactory = p_198289_.newInstance(p_198288_).forkPositional();
        this.clayBandsOffsetNoise = Noises.instantiate(p_198285_, this.randomFactory, Noises.CLAY_BANDS_OFFSET);
        this.clayBands = generateBands(this.randomFactory.fromHashOf(new ResourceLocation("clay_bands")));
        this.surfaceNoise = Noises.instantiate(p_198285_, this.randomFactory, Noises.SURFACE);
        this.surfaceSecondaryNoise = Noises.instantiate(p_198285_, this.randomFactory, Noises.SURFACE_SECONDARY);
        this.badlandsPillarNoise = Noises.instantiate(p_198285_, this.randomFactory, Noises.BADLANDS_PILLAR);
        this.badlandsPillarRoofNoise = Noises.instantiate(p_198285_, this.randomFactory, Noises.BADLANDS_PILLAR_ROOF);
        this.badlandsSurfaceNoise = Noises.instantiate(p_198285_, this.randomFactory, Noises.BADLANDS_SURFACE);
    }

    public static BlockState[] generateBands(RandomSource p_189965_)
    {
        BlockState[] ablockstate = new BlockState[192];
        Arrays.fill(ablockstate, BLUE_TERRACOTTA);

        for (int k = 0; k < ablockstate.length; ++k)
        {
            k += p_189965_.nextInt(5) + 1;
            if (k < ablockstate.length)
            {
                ablockstate[k] = PURPLE_TERRACOTTA;
            }
        }

        makeBands(p_189965_, ablockstate, 1, MAGENTA_TERRACOTTA);
        makeBands(p_189965_, ablockstate, 2, PINK_TERRACOTTA);
        makeBands(p_189965_, ablockstate, 1, PURPLE_TERRACOTTA);
        int l = p_189965_.nextIntBetweenInclusive(9, 15);
        int i = 0;

        for (int j = 0; i < l && j < ablockstate.length; j += p_189965_.nextInt(16) + 4)
        {
            ablockstate[j] = LIGHT_BLUE_TERRACOTTA;
            if (j - 1 > 0 && p_189965_.nextBoolean())
            {
                ablockstate[j - 1] = MAGENTA_TERRACOTTA;
            }

            if (j + 1 < ablockstate.length && p_189965_.nextBoolean())
            {
                ablockstate[j + 1] = MAGENTA_TERRACOTTA;
            }

            ++i;
        }

        return ablockstate;
    }

    public static void makeBands(RandomSource p_189967_, BlockState[] p_189968_, int p_189969_, BlockState p_189970_)
    {
        int i = p_189967_.nextIntBetweenInclusive(6, 15);

        for (int j = 0; j < i; ++j)
        {
            int k = p_189969_ + p_189967_.nextInt(3);
            int l = p_189967_.nextInt(p_189968_.length);

            for (int i1 = 0; l + i1 < p_189968_.length && i1 < k; ++i1)
            {
                p_189968_[l + i1] = p_189970_;
            }
        }
    }

    public int getSurfaceDepth(int p_189928_, int p_189929_)
    {
        return this.getSurfaceDepth(this.surfaceNoise, p_189928_, p_189929_);
    }

    public int getSurfaceDepth(NormalNoise p_189980_, int p_189981_, int p_189982_)
    {
        return (int) (p_189980_.getValue((double) p_189981_, 0.0D, (double) p_189982_) * 2.75D + 3.0D + this.randomFactory.at(p_189981_, 0, p_189982_).nextDouble() * 0.25D);
    }

//    public BlockState getBand(int p_189931_, int p_189932_, int p_189933_)
//    {
//        int i = (int) Math.round(this.clayBandsOffsetNoise.getValue((double) p_189931_, 0.0D, (double) p_189933_) * 4.0D);
//        return this.clayBands[(p_189932_ + i + this.clayBands.length) % this.clayBands.length];
//    }
}
