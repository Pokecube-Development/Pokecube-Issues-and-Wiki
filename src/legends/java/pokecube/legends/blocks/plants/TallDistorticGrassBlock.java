package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;

public class TallDistorticGrassBlock extends TallGrassBlock implements IForgeShearable
{
   public TallDistorticGrassBlock(final BlockBehaviour.Properties config)
   {
      super(config);
   }

   @Override
   public boolean isValidBonemealTarget(LevelReader worldReader, BlockPos pos, BlockState state, boolean b)
   {
      return false;
   }

   @Override
   public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state)
   {
      return false;
   }

   @Override
   public void performBonemeal(final ServerLevel world, final RandomSource random, final BlockPos pos, final BlockState state)
   {}
}