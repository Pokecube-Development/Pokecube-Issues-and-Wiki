package pokecube.world.gen.features.trees.trunks;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class StraightTrunkPlacerNoDirt extends TrunkPlacer
{
    public static final Codec<StraightTrunkPlacerNoDirt> CODEC = RecordCodecBuilder.create((instance) ->
    {
        return trunkPlacerParts(instance).apply(instance, StraightTrunkPlacerNoDirt::new);
    });

    public StraightTrunkPlacerNoDirt(int baseHeight, int heightRandA, int heightRandB)
    {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type()
    {
        return TrunkPlacerTypes.STRAIGHT_TRUNK_PLACER_NO_DIRT.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                                            Random random, int freeTreeHeight, BlockPos pos, TreeConfiguration treeConfig)
    {
        TrunkPlacerTypes.requireForceDirtAt(level, blockSetter, random, pos.below(), treeConfig);

        for(int i = 0; i < freeTreeHeight; ++i)
        {
            placeLog(level, blockSetter, random, pos.above(i), treeConfig);
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight), 0, false));
    }
}