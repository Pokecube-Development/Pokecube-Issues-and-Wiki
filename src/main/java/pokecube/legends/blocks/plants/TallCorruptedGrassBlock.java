package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;
import pokecube.legends.init.PlantsInit;

public class TallCorruptedGrassBlock extends TallGrassBlock implements IForgeShearable
{

   public TallCorruptedGrassBlock(BlockBehaviour.Properties config)
   {
      super(config);
   }

   @Override
   public boolean isBonemealSuccess(Level world, Random random, BlockPos pos, BlockState state)
   {
      return false;
   }

   @Override
   public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
   {
   }
}