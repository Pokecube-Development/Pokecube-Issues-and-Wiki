package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractTopPlantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlockHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import pokecube.legends.init.BlockInit;

public class DistortedVinesTopBlock extends AbstractTopPlantBlock 
{
   public static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 15.0D, 12.0D);

   public DistortedVinesTopBlock(AbstractBlock.Properties props) {
      super(props, Direction.UP, SHAPE, false, 0.1D);
   }

   protected int getBlocksToGrowWhenBonemealed(Random random) {
      return PlantBlockHelper.getBlocksToGrowWhenBonemealed(random);
   }

   protected Block getBodyBlock() {
      return BlockInit.DISTORTIC_VINES_PLANT.get();
   }

   protected boolean canGrowInto(BlockState state) {
      return PlantBlockHelper.isValidGrowthState(state);
   }
}