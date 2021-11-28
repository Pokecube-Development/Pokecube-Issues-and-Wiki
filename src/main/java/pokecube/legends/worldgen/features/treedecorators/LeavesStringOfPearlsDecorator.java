package pokecube.legends.worldgen.features.treedecorators;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import pokecube.legends.blocks.plants.StringOfPearlsBlock;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.WorldgenFeatures;

public class LeavesStringOfPearlsDecorator extends TreeDecorator
{
    public static final Codec<LeavesStringOfPearlsDecorator> CODEC;
    public static final LeavesStringOfPearlsDecorator        INSTANCE = new LeavesStringOfPearlsDecorator();

    @Override
    public TreeDecoratorType<?> type()
    {
        return WorldgenFeatures.LEAVES_STRING_OF_PEARLS.get();
    }

    @Override
    public void place(final LevelSimulatedReader world, final BiConsumer<BlockPos, BlockState> blockPos,
            final Random random, final List<BlockPos> listPos, final List<BlockPos> listPos1)
    {
        listPos1.forEach((listedPos) ->
        {
            if (random.nextInt(4) == 0)
            {
                final BlockPos pos = listedPos.west();
                if (Feature.isAir(world, pos)) LeavesStringOfPearlsDecorator.addHangingVine(world, pos, VineBlock.EAST,
                        blockPos, random);
            }
            if (random.nextInt(4) == 0)
            {
                final BlockPos pos1 = listedPos.east();
                if (Feature.isAir(world, pos1)) LeavesStringOfPearlsDecorator.addHangingVine(world, pos1,
                        VineBlock.WEST, blockPos, random);
            }
            if (random.nextInt(4) == 0)
            {
                final BlockPos pos2 = listedPos.north();
                if (Feature.isAir(world, pos2)) LeavesStringOfPearlsDecorator.addHangingVine(world, pos2,
                        VineBlock.SOUTH, blockPos, random);
            }
            if (random.nextInt(4) == 0)
            {
                final BlockPos pos3 = listedPos.south();
                if (Feature.isAir(world, pos3)) LeavesStringOfPearlsDecorator.addHangingVine(world, pos3,
                        VineBlock.NORTH, blockPos, random);
            }
        });
    }

    public static void addHangingVine(final LevelSimulatedReader world, final BlockPos pos, final BooleanProperty b,
            final BiConsumer<BlockPos, BlockState> blockPos, final Random random)
    {
        LeavesStringOfPearlsDecorator.placeVine(blockPos, pos, b, random);
        int i = 4;

        for (BlockPos pos1 = pos.below(); Feature.isAir(world, pos1) && i > 0; --i)
        {
            LeavesStringOfPearlsDecorator.placeVine(blockPos, pos1, b, random);
            pos1 = pos1.below();
        }

    }

    public static void placeVine(final BiConsumer<BlockPos, BlockState> blockPos, final BlockPos pos,
            final BooleanProperty b, final Random random)
    {
        blockPos.accept(pos, BlockInit.STRING_OF_PEARLS.get().defaultBlockState().setValue(b, Boolean.valueOf(true))
                .setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)));
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return LeavesStringOfPearlsDecorator.INSTANCE;
        });
    }
}