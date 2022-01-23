package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.PlantsInit;

public class PurpleWisteriaVinesBlock extends GrowingPlantHeadBlock implements BonemealableBlock
{
   public static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public PurpleWisteriaVinesBlock(BlockBehaviour.Properties properties)
   {
       super(properties, Direction.DOWN, SHAPE, false, 0.1D);
       this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   @Override
   protected GrowingPlantHeadBlock getHeadBlock()
   {
       return this;
   }

   @Override
   protected int getBlocksToGrowWhenBonemealed(Random random)
   {
      return NetherVines.getBlocksToGrowWhenBonemealed(random);
   }
   
   @Override
   protected boolean canGrowInto(BlockState state)
   {
      return state.isAir();
   }
   
   @Override
   protected Block getBodyBlock()
   {
      return PlantsInit.PURPLE_WISTERIA_VINES_PLANT.get();
   }
}
