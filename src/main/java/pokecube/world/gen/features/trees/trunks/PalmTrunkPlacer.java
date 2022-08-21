package pokecube.world.gen.features.trees.trunks;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class PalmTrunkPlacer extends TrunkPlacer
{
    public static final Codec<PalmTrunkPlacer> CODEC = RecordCodecBuilder.create((instance) -> {
        return trunkPlacerParts(instance).and(instance
                .group(ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_height_for_leaves", 1).forGetter((placer) ->
                {
                    return placer.minHeightForLeaves;
                }), IntProvider.codec(1, 64).fieldOf("bend_length").forGetter((placer) -> {
                    return placer.bendLength;
                }))).apply(instance, PalmTrunkPlacer::new);
    });

    private final int minHeightForLeaves;
    private final IntProvider bendLength;

    public PalmTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, int minHeightForLeaves,
                           IntProvider bendLength)
    {
        super(baseHeight, heightRandA, heightRandB);
        this.minHeightForLeaves = minHeightForLeaves;
        this.bendLength = bendLength;
    }

    @Override
    protected TrunkPlacerType<?> type()
    {
        return TrunkPlacerTypes.PALM_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                                            Random random, int freeTreeHeight, BlockPos pos, TreeConfiguration config)
    {
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int i = freeTreeHeight - 1;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        BlockPos blockpos = blockpos$mutableblockpos.below();
        TrunkPlacerTypes.setDirtAt(level, blockSetter, random, blockpos, config);
        List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();

        for (int j = 0; j <= i; ++j)
        {
            if (j + 2 >= i + random.nextInt(2))
            {
                blockpos$mutableblockpos.move(direction);
            }

            if (TreeFeature.validTreePos(level, blockpos$mutableblockpos))
            {
                placeLog(level, blockSetter, random, blockpos$mutableblockpos, config);
            }

            blockpos$mutableblockpos.move(Direction.UP);
        }

        int l = this.bendLength.sample(random);

        for (int k = 0; k <= l; ++k)
        {
            if (TreeFeature.validTreePos(level, blockpos$mutableblockpos))
            {
                placeLog(level, blockSetter, random, blockpos$mutableblockpos, config);
            }
            blockpos$mutableblockpos.move(direction);
        }
        blockpos$mutableblockpos.move(direction.getOpposite());
        list.add(new FoliagePlacer.FoliageAttachment(blockpos$mutableblockpos.immutable().above(), 0, false));
        return list;
    }
}