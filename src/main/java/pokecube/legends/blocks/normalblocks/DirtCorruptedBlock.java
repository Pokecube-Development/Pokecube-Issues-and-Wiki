package pokecube.legends.blocks.normalblocks;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.legends.init.BlockInit;

public class DirtCorruptedBlock extends Block implements IGrowable
{
    public DirtCorruptedBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(final IBlockReader block, final BlockPos pos, final BlockState state,
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
            while (!block.getBlockState(blockpos).is(BlockInit.ULTRA_CORRUPTED_GRASS.get()));

            return true;
        }
    }

    @Override
    public boolean isBonemealSuccess(final World world, final Random random, final BlockPos pos, final BlockState state)
    {
        return true;
    }

    @Override
    public void performBonemeal(final ServerWorld world, final Random random, final BlockPos pos,
            final BlockState state)
    {
        boolean valid = false;
        final Iterator<BlockPos> var7 = BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)).iterator();

        while (var7.hasNext())
        {
            final BlockPos blockpos = var7.next();
            final BlockState state1 = world.getBlockState(blockpos);
            if (state1.is(BlockInit.ULTRA_CORRUPTED_GRASS.get())) valid = true;

            if (valid) break;
        }

        if (valid) world.setBlock(pos, BlockInit.ULTRA_CORRUPTED_GRASS.get().defaultBlockState().setValue(
                GrassCorruptedBlock.SNOWY, world.getBlockState(pos.above()).is(Blocks.SNOW)), 3);
    }
}
