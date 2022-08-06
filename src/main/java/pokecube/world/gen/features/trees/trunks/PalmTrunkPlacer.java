package pokecube.world.gen.features.trees.trunks;

import com.google.common.collect.ImmutableList;
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

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class PalmTrunkPlacer extends TrunkPlacer
{
    private final int minHeightForLeaves;
    private final IntProvider bendLength;
    public static final Codec<PalmTrunkPlacer> CODEC = RecordCodecBuilder.create((type) ->
    {
        return trunkPlacerParts(type).and(type.group(ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_height_for_leaves", 1)
                .forGetter((get) ->
                {
                    return get.minHeightForLeaves;
                }), IntProvider.codec(1, 64).fieldOf("bend_length").forGetter((get) ->
        {
            return get.bendLength;
        }))).apply(type, PalmTrunkPlacer::new);
    });

    public PalmTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, int minHeightForLeaves, IntProvider bendLength)
    {
        super(baseHeight, heightRandA, heightRandB);
        this.minHeightForLeaves = minHeightForLeaves;
        this.bendLength = bendLength;
    }

    protected TrunkPlacerType<?> type() {
        return TrunkPlacerTypes.PALM_TRUNK_PLACER.get();
    }

    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, Random random,
                                                            int freeTreeHeight, BlockPos pos, TreeConfiguration treeConfig)
    {
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int i = freeTreeHeight - 1;
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        BlockPos posBelow = mutablePos.below();
        setDirtAt(level, blockSetter, random, posBelow, treeConfig);
        List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();

        for(int j = 0; j <= i; ++j)
        {
            if (j + 1 >= i + random.nextInt(2))
            {
                mutablePos.move(direction);
            }

            if (TreeFeature.validTreePos(level, mutablePos))
            {
                placeLog(level, blockSetter, random, mutablePos, treeConfig);
            }

            mutablePos.move(Direction.UP);
        }

        int l = this.bendLength.sample(random);

        for(int k = 0; k <= l; ++k)
        {
            if (TreeFeature.validTreePos(level, mutablePos))
            {
                placeLog(level, blockSetter, random, mutablePos, treeConfig);
            }
            mutablePos.move(direction);
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.below().above(freeTreeHeight), 0, false));
    }
}
