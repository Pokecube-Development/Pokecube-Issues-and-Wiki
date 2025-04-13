package pokecube.legends.worldgen.trees.treedecorators;

import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import pokecube.legends.blocks.plants.StringOfPearlsBlock;
import pokecube.legends.init.PlantsInit;
import pokecube.legends.worldgen.trees.Trees;

public class TrunkStringOfPearlsDecorator extends TreeDecorator
{
    public static final MapCodec<TrunkStringOfPearlsDecorator> CODEC;
    public static final TrunkStringOfPearlsDecorator INSTANCE = new TrunkStringOfPearlsDecorator();

    protected TreeDecoratorType<?> type()
    {
        return Trees.TRUNK_STRING_OF_PEARLS.get();
    }

    @Override
    public void place(TreeDecorator.Context context)
    {
        RandomSource random = context.random();
        var world = context.level();
        BiConsumer<BlockPos, BlockState> blockSetter = context::setBlock;
        context.logs().forEach((listedPos) -> {
            if (random.nextInt(3) > 0)
            {
                BlockPos pos = listedPos.west();
                if (world.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::isAir))
                {
                    placeVine(blockSetter, pos, StringOfPearlsBlock.EAST, random);
                }
            }

            if (random.nextInt(3) > 0)
            {
                BlockPos pos = listedPos.east();
                if (world.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::isAir))
                {
                    placeVine(blockSetter, pos, StringOfPearlsBlock.WEST, random);
                }
            }

            if (random.nextInt(3) > 0)
            {
                BlockPos pos = listedPos.north();
                if (world.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::isAir))
                {
                    placeVine(blockSetter, pos, StringOfPearlsBlock.SOUTH, random);
                }
            }

            if (random.nextInt(3) > 0)
            {
                BlockPos pos = listedPos.south();
                if (world.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::isAir))
                {
                    placeVine(blockSetter, pos, StringOfPearlsBlock.NORTH, random);
                }
            }

        });
    }

    public static void placeVine(BiConsumer<BlockPos, BlockState> blockSetter, BlockPos pos, BooleanProperty b,
            RandomSource random)
    {
        blockSetter.accept(pos, PlantsInit.STRING_OF_PEARLS.get().defaultBlockState().setValue(b, Boolean.TRUE)
                .setValue(StringOfPearlsBlock.FLOWERS, random.nextFloat() < 0.11F));
    }

    static
    {
        CODEC = MapCodec.unit(() -> {
            return INSTANCE;
        });
    }
}