package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;

public class CorruptedDirtBlock extends Block implements BonemealableBlock
{
    // Tag
    public static final TagKey<Block> CORRUPTED_GRASS_SPREADABLE = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation(Reference.ID, "corrupted_grass_spreadable"));

    public CorruptedDirtBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter block, BlockPos pos, BlockState state, boolean b)
    {
       if (!block.getBlockState(pos.above()).propagatesSkylightDown(block, pos))
       {
          return false;
       } else
       {
          for(BlockPos posOffset : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)))
          {
             if (block.getBlockState(posOffset).is(CORRUPTED_GRASS_SPREADABLE))
             {
                return true;
             }
          }
          return false;
       }
    }

    @Override
    public boolean isBonemealSuccess(final Level world, final RandomSource random, final BlockPos pos, final BlockState state)
    {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state)
    {
        boolean flag1 = false;

        for(BlockPos posOffset : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)))
        {
           BlockState stateOffset = world.getBlockState(posOffset);
           if (stateOffset.is(BlockInit.CORRUPTED_GRASS_BLOCK.get()))
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
           world.setBlock(pos, BlockInit.CORRUPTED_GRASS_BLOCK.get().defaultBlockState(), 3);
        }
    }
}
