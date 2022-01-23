package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;
import pokecube.legends.init.PlantsInit;

public class TallCorruptedGrassBlock extends TallGrassBlock implements IForgeShearable
{
   public TallCorruptedGrassBlock(final BlockBehaviour.Properties config)
   {
      super(config);
   }

   @Override
   public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos, final BlockState state)
   {
       final DoublePlantBlock block = (DoublePlantBlock) PlantsInit.TALL_CORRUPTED_GRASS.get();
       if (block.defaultBlockState().canSurvive(world, pos) && world.isEmptyBlock(pos.above())) 
           DoublePlantBlock.placeAt(world, block.defaultBlockState(), pos, 2);
   }
}