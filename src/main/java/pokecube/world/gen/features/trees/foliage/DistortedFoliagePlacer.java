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

public class DistortedFoliagePlacer extends FoliagePlacer
{
    protected final IntProvider height;

    public static final Codec<DistortedFoliagePlacer> CODEC = RecordCodecBuilder.create((type) -> {
        return distortedParts(type).apply(type, DistortedFoliagePlacer::new);
    });

    protected static <P extends DistortedFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, IntProvider> distortedParts(
            RecordCodecBuilder.Instance<P> instance)
    {
        return foliagePlacerParts(instance).and(IntProvider.codec(0, 16).fieldOf("height").forGetter((get) -> {
            return get.height;
        }));
    }

    public DistortedFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height)
    {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type()
    {
        return FoliagePlacerTypes.DISTORTED_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                 Random random, TreeConfiguration treeConfig, int maxFreeTreeHeight,
                                 FoliageAttachment foliageAttachment, int height, int radius, int offset)
    {
        for(int i = offset; i >= offset - height; --i) {
            int j = radius + (i != offset && i != offset - height ? 1 : 0);
            this.placeLeavesRow(level, blockSetter, random, treeConfig, foliageAttachment.pos(), j, i, foliageAttachment.doubleTrunk());
            this.placeLeavesDot(level, blockSetter, random, treeConfig, foliageAttachment.pos(), j - 1, i - 4, foliageAttachment.doubleTrunk());
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
                if(j + k <= range - 1)
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

    protected void placeLeavesDot(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                  Random random, TreeConfiguration treeConfig, BlockPos pos, int range, int yOffset, boolean large)
    {
        int i = large ? 1 : 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for(int j = -range; j <= range + i; ++j)
        {
            for(int k = -range; k <= range + i; ++k)
            {
                if(j * j + k * k <= (range * range) + (range / 8))
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
