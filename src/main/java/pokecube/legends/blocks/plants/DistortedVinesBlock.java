package pokecube.legends.blocks.plants;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBodyPlantBlock;
import net.minecraft.block.AbstractTopPlantBlock;
import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import pokecube.legends.init.BlockInit;

public class DistortedVinesBlock extends AbstractBodyPlantBlock 
{
   public static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

   public DistortedVinesBlock(AbstractBlock.Properties properties) {
	   super(properties, Direction.UP, SHAPE, false);
   }

   protected AbstractTopPlantBlock getHeadBlock() {
      return (AbstractTopPlantBlock)BlockInit.DISTORTIC_VINES.get();
   }
}
