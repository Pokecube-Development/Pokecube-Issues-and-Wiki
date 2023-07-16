package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PlantsInit;

public class DistortedVinesTopBlock extends GrowingPlantHeadBlock
{
   public static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D);

   public DistortedVinesTopBlock(BlockBehaviour.Properties props)
   {
      super(props, Direction.UP, SHAPE, false, 0.1D);
   }

   @Override
   protected int getBlocksToGrowWhenBonemealed(Random random)
   {
      return NetherVines.getBlocksToGrowWhenBonemealed(random);
   }

   @Override
   protected Block getBodyBlock()
   {
      return PlantsInit.DISTORTIC_VINES_PLANT.get();
   }

   @Override
   protected boolean canGrowInto(BlockState state)
   {
      return NetherVines.isValidGrowthState(state);
   }

   @Override
   public ItemStack getCloneItemStack(BlockGetter block, BlockPos pos, BlockState state)
   {
      return new ItemStack(ItemInit.DISTORTIC_VINES.get());
   }
}