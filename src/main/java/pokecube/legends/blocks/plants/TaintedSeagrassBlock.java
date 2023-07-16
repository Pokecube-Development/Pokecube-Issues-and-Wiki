package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.common.IForgeShearable;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;

public class TaintedSeagrassBlock extends SeagrassBlock implements BonemealableBlock, LiquidBlockContainer, IForgeShearable
{
   public TaintedSeagrassBlock(final BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos, final BlockState state)
   {
      final BlockState state1 = PlantsInit.TALL_TAINTED_SEAGRASS.get().defaultBlockState();
      final BlockState state2 = state1.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
      final BlockPos blockpos = pos.above();
      if (world.getBlockState(blockpos).is(Blocks.WATER))
      {
         world.setBlock(pos, state1, 2);
         world.setBlock(blockpos, state2, 2);
      }
    else Block.popResource(world, pos, new ItemStack(this));
   }

   @Override
   public boolean mayPlaceOn(final BlockState state, final BlockGetter block, final BlockPos pos)
   {
      return (state.isFaceSturdy(block, pos, Direction.UP) || state.is(BlockInit.CRYSTALLIZED_SAND.get())) && !state.is(Blocks.MAGMA_BLOCK);
   }
}
