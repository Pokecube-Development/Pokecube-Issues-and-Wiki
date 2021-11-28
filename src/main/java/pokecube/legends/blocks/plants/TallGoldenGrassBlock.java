package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;
import pokecube.legends.init.PlantsInit;

public class TallGoldenGrassBlock extends TallGrassBlock implements BonemealableBlock, IForgeShearable
{

   public TallGoldenGrassBlock(BlockBehaviour.Properties config)
   {
      super(config);
   }

   @Override
   public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
   {
      DoublePlantBlock block = (DoublePlantBlock)(state.is(PlantsInit.GOLDEN_FERN.get()) ?
              PlantsInit.LARGE_GOLDEN_FERN.get() : PlantsInit.TALL_GOLDEN_GRASS.get());
      if (block.defaultBlockState().canSurvive(world, pos) && world.isEmptyBlock(pos.above()))
      {
         DoublePlantBlock.placeAt(world, block.defaultBlockState(), pos, 2);
      }
   }
}