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
    protected final int height;

    public static final Codec<PalmFoliagePlacer> CODEC = RecordCodecBuilder.create((type) -> {
        return palmParts(type).apply(type, PalmFoliagePlacer::new);
    });

    protected static <P extends PalmFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, Integer> palmParts(
            RecordCodecBuilder.Instance<P> instance)
    {
        return foliagePlacerParts(instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter((get) -> {
            return get.height;
        }));
    }

    public PalmFoliagePlacer(IntProvider radius, IntProvider offset, int height)
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
            int range = Math.max(radius + foliageAttachment.radiusOffset() - 1 - yOffset, 0);
            placeLeavesRow(level, blockSetter, random, treeConfig, foliageAttachment.pos(), range, yOffset,
                    foliageAttachment.doubleTrunk());
        }
    }

    @Override
    protected void placeLeavesRow(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
            Random random, TreeConfiguration treeConfig, BlockPos pos, int range, int yOffset, boolean large)
    {
        int minRadius = range - yOffset - 1 + (yOffset % 2);
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
        }
    }

    @Override
    public int foliageHeight(Random random, int height, TreeConfiguration treeConfig)
    {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int localX, int localY, int localZ, int range, boolean large)
    {
        return localX == range && localZ == range && (random.nextInt(4) == 0 || localY == 0);
    }
}
