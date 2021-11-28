package pokecube.legends.blocks.plants;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import pokecube.legends.init.PlantsInit;

public class TaintedKelpBlock extends KelpBlock implements LiquidBlockContainer
{
   public TaintedKelpBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public Block getBodyBlock()
   {
      return PlantsInit.TAINTED_KELP_PLANT.get();
   }
}