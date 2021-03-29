package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.legends.init.BlockInit;

import java.util.Iterator;
import java.util.Random;

public class DistorticStoneBlock extends Block implements IGrowable
{
    public DistorticStoneBlock(final String name, final Properties properties)
    {
        super(properties);
    }

    public boolean isValidBonemealTarget(IBlockReader block, BlockPos pos, BlockState state, boolean valid)
    {
        if (!block.getBlockState(pos.above()).propagatesSkylightDown(block, pos))
        {
            return false;
        } else {
            Iterator var5 = BlockPos.betweenClosed(pos.offset(-1, -1, -1),
                pos.offset(1, 1, 1)).iterator();

            BlockPos blockpos;
            do {
                if (!var5.hasNext())
                {
                    return false;
                }

                blockpos = (BlockPos)var5.next();
            } while(!block.getBlockState(blockpos).is(BlockInit.DISTORTIC_GRASS.get()));

            return true;
        }
    }

    public boolean isBonemealSuccess(World world, Random random, BlockPos pos, BlockState state)
    {
        return true;
    }


    public void performBonemeal(ServerWorld world, Random random, BlockPos pos, BlockState state)
    {
        boolean valid = false;
        Iterator var7 = BlockPos.betweenClosed(pos.offset(-1, -1, -1),
            pos.offset(1, 1, 1)).iterator();

        while(var7.hasNext())
        {
            BlockPos blockpos = (BlockPos)var7.next();
            BlockState state1 = world.getBlockState(blockpos);
            if (state1.is(BlockInit.DISTORTIC_GRASS.get()))
            {
                valid = true;
            }

            if (valid)
            {
                break;
            }
        }

        if (valid)
        {
            world.setBlock(pos, BlockInit.DISTORTIC_GRASS.get().defaultBlockState()
                .setValue(GrassCorruptedBlock.SNOWY, world.getBlockState(pos.above()).is(Blocks.SNOW)), 3);
        }
    }
}
