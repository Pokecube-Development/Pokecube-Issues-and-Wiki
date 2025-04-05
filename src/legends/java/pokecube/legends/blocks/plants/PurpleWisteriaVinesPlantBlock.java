package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.PlantsInit;

public class PurpleWisteriaVinesPlantBlock extends GrowingPlantBodyBlock implements BonemealableBlock
{
   public static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public PurpleWisteriaVinesPlantBlock(BlockBehaviour.Properties properties)
   {
       super(properties, Direction.DOWN, SHAPE, false);
   }

   @Override
   protected GrowingPlantHeadBlock getHeadBlock()
   {
      return (GrowingPlantHeadBlock)PlantsInit.PURPLE_WISTERIA_VINES.get();
   }
   
   @Override
   public ItemStack getCloneItemStack(BlockGetter block, BlockPos pos, BlockState state)
   {
       return new ItemStack(PlantsInit.PURPLE_WISTERIA_VINES.get());
   }
}
