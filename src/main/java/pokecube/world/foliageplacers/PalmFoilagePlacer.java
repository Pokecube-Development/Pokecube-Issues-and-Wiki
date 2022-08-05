package pokecube.world.foliageplacers;

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

import java.util.Random;
import java.util.function.BiConsumer;

public class PalmFoilagePlacer extends FoliagePlacer {

    protected final int height;

    public static final Codec<PalmFoilagePlacer> CODEC = RecordCodecBuilder.create((type) ->
    {
        return palmParts(type).apply(type, PalmFoilagePlacer::new);
    });

    protected static <P extends PalmFoilagePlacer> P3<Mu<P>, IntProvider, IntProvider, Integer> palmParts(RecordCodecBuilder.Instance<P> instance)
    {
        return foliagePlacerParts(instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter((get) ->
        {
            return get.height;
        }));
    }

    public PalmFoilagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerTypes.PALM_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, Random random,
                                 TreeConfiguration treeConfig, int maxFreeTreeHeight, FoliagePlacer.FoliageAttachment foliageAttachment, int height, int radius, int offset)
    {
        for(int i = offset; i >= offset - height; --i)
        {
            int j = Math.max(radius + foliageAttachment.radiusOffset() - 1 - i / 2, 0);
            placeLeavesRow(level, blockSetter, random, treeConfig, foliageAttachment.pos(), j, i, foliageAttachment.doubleTrunk());
        }
    }

    @Override
    protected void placeLeavesRow(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, Random random,
                                  TreeConfiguration treeConfig, BlockPos pos, int range, int yOffset, boolean large)
    {
        int i = large ? 1 : 0;
        int minRadius = 1;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for(int j = -range; j <= range + i; ++j)
        {
            if (j < Math.abs(minRadius)) continue;
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
