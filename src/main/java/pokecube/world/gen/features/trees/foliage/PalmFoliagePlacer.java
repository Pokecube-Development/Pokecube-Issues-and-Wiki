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

public class PalmFoliagePlacer extends FoliagePlacer
{
    protected final IntProvider height;

    public static final Codec<PalmFoliagePlacer> CODEC = RecordCodecBuilder.create((type) -> {
        return palmParts(type).apply(type, PalmFoliagePlacer::new);
    });

    protected static <P extends PalmFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, IntProvider> palmParts(
            RecordCodecBuilder.Instance<P> instance)
    {
        return foliagePlacerParts(instance).and(IntProvider.codec(0, 16).fieldOf("height").forGetter((get) -> {
            return get.height;
        }));
    }

    public PalmFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height)
    {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type()
    {
        return FoliagePlacerTypes.PALM_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
            Random random, TreeConfiguration treeConfig, int maxFreeTreeHeight,
            FoliagePlacer.FoliageAttachment foliageAttachment, int height, int radius, int offset)
    {
        for (int yOffset = offset; yOffset >= offset - height; --yOffset)
        {
            int range = Math.max(radius + foliageAttachment.radiusOffset() - 2 - yOffset, 0);
            placeLeafSegment(level, blockSetter, random, treeConfig, foliageAttachment.pos(), offset, range, yOffset,
                    foliageAttachment.doubleTrunk());
        }
    }

    protected void placeLeafSegment(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
            Random random, TreeConfiguration treeConfig, BlockPos pos, int offset, int range, int yOffset,
            boolean large)
    {
        int minRadius = range - yOffset - 2 + ((1 + yOffset) % 2);
        if (yOffset == offset) minRadius = 0;
        int sideLeafRadius = 1;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int j = -range; j <= range; ++j)
        {
            if (minRadius > Math.abs(j)) continue;
            if (!this.shouldSkipLocationSigned(random, j, yOffset, 0, range, large))
            {
                mutablePos.setWithOffset(pos, j, yOffset, 0);
                tryPlaceLeaf(level, blockSetter, random, treeConfig, mutablePos);
            }
            if (!this.shouldSkipLocationSigned(random, 0, yOffset, j, range, large))
            {
                mutablePos.setWithOffset(pos, 0, yOffset, j);
                tryPlaceLeaf(level, blockSetter, random, treeConfig, mutablePos);
            }
            if (minRadius <= sideLeafRadius && Math.abs(j) <= sideLeafRadius)
            {
                for (int k = -sideLeafRadius; k <= sideLeafRadius; ++k)
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
        return localX == range && localZ == range && (random.nextInt(4) == 0 || localY == 0);
    }
}
