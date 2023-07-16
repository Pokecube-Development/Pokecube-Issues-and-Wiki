package pokecube.legends.blocks.normalblocks;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.init.BlockInit;

public class DistorticStoneBlock extends Block implements BonemealableBlock
{
    public DistorticStoneBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(final BlockGetter block, final BlockPos pos, final BlockState state,
            final boolean valid)
    {
        if (!block.getBlockState(pos.above()).propagatesSkylightDown(block, pos)) return false;
        else
        {
            final Iterator<BlockPos> var5 = BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
                    .iterator();

            BlockPos blockpos;
            do
            {
                if (!var5.hasNext()) return false;

                blockpos = var5.next();
            }
            while (!block.getBlockState(blockpos).is(BlockInit.DISTORTIC_GRASS_BLOCK.get()));

            return true;
        }
    }

    @Override
    public boolean isBonemealSuccess(final Level world, final Random random, final BlockPos pos, final BlockState state)
    {
        return true;
    }

    @Override
    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos,
            final BlockState state)
    {
        boolean valid = false;
        final Iterator<BlockPos> var7 = BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)).iterator();

        while (var7.hasNext())
        {
            final BlockPos blockpos = var7.next();
            final BlockState state1 = world.getBlockState(blockpos);
            if (state1.is(BlockInit.DISTORTIC_GRASS_BLOCK.get())) valid = true;

            if (valid) break;
        }

        if (valid) world.setBlock(pos, BlockInit.DISTORTIC_GRASS_BLOCK.get().defaultBlockState().setValue(
                CorruptedGrassBlock.SNOWY, world.getBlockState(pos.above()).is(Blocks.SNOW)), 3);
    }
}
