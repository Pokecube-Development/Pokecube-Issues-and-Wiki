package pokecube.legends.blocks.plants;

import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.KelpPlantBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import pokecube.legends.init.PlantsInit;

public class TaintedKelpPlantBlock extends KelpPlantBlock implements LiquidBlockContainer
{
   public TaintedKelpPlantBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public GrowingPlantHeadBlock getHeadBlock()
   {
      return (GrowingPlantHeadBlock)PlantsInit.TAINTED_KELP.get();
   }
}