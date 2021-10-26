package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.BlockInit;

public class DistortedVinesTopBlock extends GrowingPlantHeadBlock 
{
   public static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D);

   public DistortedVinesTopBlock(BlockBehaviour.Properties props) {
      super(props, Direction.UP, SHAPE, false, 0.1D);
   }

   protected int getBlocksToGrowWhenBonemealed(Random random) {
      return NetherVines.getBlocksToGrowWhenBonemealed(random);
   }

   protected Block getBodyBlock() {
      return BlockInit.DISTORTIC_VINES_PLANT.get();
   }

   protected boolean canGrowInto(BlockState state) {
      return NetherVines.isValidGrowthState(state);
   }
}