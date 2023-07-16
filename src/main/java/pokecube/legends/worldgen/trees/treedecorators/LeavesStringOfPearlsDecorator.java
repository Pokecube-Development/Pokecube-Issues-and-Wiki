package pokecube.legends.worldgen.trees.treedecorators;

import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import pokecube.legends.blocks.plants.StringOfPearlsBlock;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.trees.Trees;

public class LeavesStringOfPearlsDecorator extends TreeDecorator
{
    public static final Codec<LeavesStringOfPearlsDecorator> CODEC;
    public static final LeavesStringOfPearlsDecorator INSTANCE = new LeavesStringOfPearlsDecorator();

    @Override
    public TreeDecoratorType<?> type()
    {
        return Trees.LEAVES_STRING_OF_PEARLS.get();
    }

    @Override
    public void place(TreeDecorator.Context context)
    {
        RandomSource random = context.random();
        var world = context.level();
        BiConsumer<BlockPos, BlockState> blockSetter = context::setBlock;
        context.leaves().forEach((listedPos) -> {
            if (random.nextInt(4) == 0)
            {
                final BlockPos pos = listedPos.west();
                if (world.isStateAtPosition(pos, s -> s.isAir()))
                    LeavesStringOfPearlsDecorator.addHangingVine(world, pos, VineBlock.EAST, blockSetter, random);
            }
            if (random.nextInt(4) == 0)
            {
                final BlockPos pos = listedPos.east();
                if (world.isStateAtPosition(pos, s -> s.isAir()))
                    LeavesStringOfPearlsDecorator.addHangingVine(world, pos, VineBlock.WEST, blockSetter, random);
            }
            if (random.nextInt(4) == 0)
            {
                final BlockPos pos = listedPos.north();
                if (world.isStateAtPosition(pos, s -> s.isAir()))
                    LeavesStringOfPearlsDecorator.addHangingVine(world, pos, VineBlock.SOUTH, blockSetter, random);
            }
            if (random.nextInt(4) == 0)
            {
                final BlockPos pos = listedPos.south();
                if (world.isStateAtPosition(pos, s -> s.isAir()))
                    LeavesStringOfPearlsDecorator.addHangingVine(world, pos, VineBlock.NORTH, blockSetter, random);
            }
        });
    }

    public static void addHangingVine(final LevelSimulatedReader world, final BlockPos pos, final BooleanProperty b,
            final BiConsumer<BlockPos, BlockState> blockSetter, final RandomSource random)
    {
        LeavesStringOfPearlsDecorator.placeVine(blockSetter, pos, b, random);
        int i = 4;

        for (BlockPos pos1 = pos.below(); world.isStateAtPosition(pos1, s -> s.isAir()) && i > 0; --i)
        {
            LeavesStringOfPearlsDecorator.placeVine(blockSetter, pos1, b, random);
            pos1 = pos1.below();
        }

    }

    public static void placeVine(final BiConsumer<BlockPos, BlockState> blockPos, final BlockPos pos,
            final BooleanProperty b, final RandomSource random)
    {
        blockPos.accept(pos, BlockInit.STRING_OF_PEARLS.get().defaultBlockState().setValue(b, Boolean.valueOf(true))
                .setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)));
    }

    static
    {
        CODEC = Codec.unit(() -> {
            return LeavesStringOfPearlsDecorator.INSTANCE;
        });
    }
}