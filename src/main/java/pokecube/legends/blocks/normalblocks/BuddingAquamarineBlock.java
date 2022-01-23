package pokecube.legends.blocks.normalblocks;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import pokecube.legends.init.BlockInit;

public class BuddingAquamarineBlock extends BuddingAmethystBlock
{
    public static final int GROWTH_CHANCE = 5;
    public static final Direction[] DIRECTIONS = Direction.values();
    
    public BuddingAquamarineBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
       if (random.nextInt(5) == 0)
       {
          Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
          BlockPos pos1 = pos.relative(direction);
          BlockState state1 = world.getBlockState(pos1);
          Block block = null;
          if (canClusterGrowAtState(state1))
          {
             block = BlockInit.SMALL_AQUAMARINE_BUD.get();
          } else if (state1.is(BlockInit.SMALL_AQUAMARINE_BUD.get()) && state1.getValue(AmethystClusterBlock.FACING) == direction)
          {
             block = BlockInit.MEDIUM_AQUAMARINE_BUD.get();
          } else if (state1.is(BlockInit.MEDIUM_AQUAMARINE_BUD.get()) && state1.getValue(AmethystClusterBlock.FACING) == direction)
          {
             block = BlockInit.LARGE_AQUAMARINE_BUD.get();
          } else if (state1.is(BlockInit.LARGE_AQUAMARINE_BUD.get()) && state1.getValue(AmethystClusterBlock.FACING) == direction)
          {
             block = BlockInit.AQUAMARINE_CLUSTER.get();
          }

          if (block != null)
          {
             BlockState state2 = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction)
                     .setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(state1.getFluidState().getType() == Fluids.WATER));
             world.setBlockAndUpdate(pos1, state2);
          }
       }
    }
}
