package pokecube.world.gen.features.trees.foliage;

import java.util.Random;
import java.util.function.BiConsumer;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class RainDropFoliagePlacer extends FoliagePlacer
{
    protected final IntProvider height;

    public static final Codec<RainDropFoliagePlacer> CODEC = RecordCodecBuilder.create((type) -> {
        return roundParts(type).apply(type, RainDropFoliagePlacer::new);
    });

    protected static <P extends RainDropFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, IntProvider> roundParts(
            RecordCodecBuilder.Instance<P> instance)
    {
        return foliagePlacerParts(instance).and(IntProvider.codec(0, 16).fieldOf("height").forGetter((get) -> {
            return get.height;
        }));
    }

    public RainDropFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height)
    {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type()
    {
        return FoliagePlacerTypes.RAIN_DROP_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                 Random random, TreeConfiguration treeConfig, int maxFreeTreeHeight,
                                 FoliageAttachment foliageAttachment, int height, int radius, int offset)
    {
        double ch = height / 2.0;
        double scale = Math.sqrt(2);
        for(int i = 0; i >= -height; --i)
        {
            int range = (int) ((Math.sqrt(ch - Math.abs(-i - ch)) + 1) * scale + 1);
            this.placeLeavesRow(level, blockSetter, random, treeConfig, foliageAttachment.pos(), range - 2, offset + i, foliageAttachment.doubleTrunk());
            this.placeLeavesRow(level, blockSetter, random, treeConfig, foliageAttachment.pos(), range - 1, offset + i - 3, foliageAttachment.doubleTrunk());
        }
    }

    protected void placeLeavesRow(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                  Random random, TreeConfiguration treeConfig, BlockPos pos, int range, int yOffset, boolean large)
    {
        int i = large ? 1 : 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for(int j = -range; j <= range + i; ++j)
        {
            for(int k = -range; k <= range + i; ++k)
            {
                if(j * j + k * k <= range * range)
                {
                    if (!this.shouldSkipLocationSigned(random, j, yOffset, k, range, large))
                    {
                        mutablePos.setWithOffset(pos, j, yOffset, k);
                        tryPlaceLeaf(level, blockSetter, random, treeConfig, mutablePos);
                    }
                }
            }
        }
    }

    @Override
    public int foliageHeight(Random random, int height, TreeConfiguration treeConfig)
    {
        return this.height.sample(random);
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int localX, int localY, int localZ, int range, boolean large)
    {
        return localX == range && localZ == range && (random.nextInt(8) == 0 || localY == 0);
    }
}
