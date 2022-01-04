package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.init.BlockInit;

public class CorruptedDirtBlock extends Block implements BonemealableBlock
{
    public CorruptedDirtBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
//    public boolean isValidBonemealTarget(final BlockGetter block, final BlockPos pos, final BlockState state,
//            final boolean valid)
//    {
//        if (!block.getBlockState(pos.above()).propagatesSkylightDown(block, pos)) return false;
//        else
//        {
//            final Iterator<BlockPos> var5 = BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
//                    .iterator();
//
//            BlockPos blockpos;
//            do
//            {
//                if (!var5.hasNext()) return false;
//
//                blockpos = var5.next();
//            }
//            while (!block.getBlockState(blockpos).is(BlockInit.CORRUPTED_GRASS.get()));
//
//            return true;
//        }
//    }
    public boolean isValidBonemealTarget(BlockGetter block, BlockPos pos, BlockState state, boolean b)
    {
       if (!block.getBlockState(pos.above()).propagatesSkylightDown(block, pos))
       {
          return false;
       } else
       {
          for(BlockPos posOffset : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)))
          {
             if (block.getBlockState(posOffset).is(BlockTags.DIRT))
             {
                return true;
             }
          }
          return false;
       }
    }

    @Override
    public boolean isBonemealSuccess(final Level world, final Random random, final BlockPos pos, final BlockState state)
    {
        return true;
    }

    @Override
//    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos,
//            final BlockState state)
//    {
//        boolean valid = false;
//        final Iterator<BlockPos> var7 = BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)).iterator();
//
//        while (var7.hasNext())
//        {
//            final BlockPos blockpos = var7.next();
//            final BlockState state1 = world.getBlockState(blockpos);
//            if (state1.is(BlockInit.CORRUPTED_GRASS.get())) valid = true;
//
//            if (valid) break;
//        }
//
//        if (valid) world.setBlock(pos, BlockInit.CORRUPTED_GRASS.get().defaultBlockState().setValue(
//                CorruptedGrassBlock.SNOWY, world.getBlockState(pos.above()).is(Blocks.SNOW)), 3);
//    }
    public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
    {
        boolean flag1 = false;

        for(BlockPos posOffset : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)))
        {
           BlockState stateOffset = world.getBlockState(posOffset);
           if (stateOffset.is(BlockInit.CORRUPTED_GRASS.get()))
           {
              flag1 = true;
           }

           if (flag1)
           {
              break;
           }
        }

        if (flag1)
        {
           world.setBlock(pos, BlockInit.CORRUPTED_GRASS.get().defaultBlockState(), 3);
        }
     }
}
