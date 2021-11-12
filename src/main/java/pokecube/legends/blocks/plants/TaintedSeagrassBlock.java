package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.PlantsInit;

public class TaintedSeagrassBlock extends SeagrassBlock implements BonemealableBlock, LiquidBlockContainer, IForgeShearable
{
   public TaintedSeagrassBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
   {
      BlockState state1 = PlantsInit.TALL_TAINTED_SEAGRASS.get().defaultBlockState();
      BlockState state2 = state1.setValue(TallTaintedSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
      BlockPos blockpos = pos.above();
      if (world.getBlockState(blockpos).is(Blocks.WATER))
      {
         world.setBlock(pos, state1, 2);
         world.setBlock(blockpos, state2, 2);
      } else
      {
          popResource(world, pos, new ItemStack(this));
      }
   }
}
