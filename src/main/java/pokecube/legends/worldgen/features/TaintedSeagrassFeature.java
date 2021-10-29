package pokecube.legends.worldgen.features;

import java.util.Random;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import pokecube.legends.init.BlockInit;

public class TaintedSeagrassFeature extends Feature<ProbabilityFeatureConfiguration>
{
   public TaintedSeagrassFeature(Codec<ProbabilityFeatureConfiguration> config)
   {
      super(config);
   }

   public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> config)
   {
      boolean flag = false;
      Random random = config.random();
      WorldGenLevel world = config.level();
      BlockPos pos = config.origin();
      ProbabilityFeatureConfiguration probConfig = config.config();
      int i = random.nextInt(8) - random.nextInt(8);
      int j = random.nextInt(8) - random.nextInt(8);
      int k = world.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX() + i, pos.getZ() + j);
      BlockPos pos1 = new BlockPos(pos.getX() + i, k, pos.getZ() + j);
      if (world.getBlockState(pos1).is(Blocks.WATER))
      {
         boolean flag1 = random.nextDouble() < (double)probConfig.probability;
         BlockState state = flag1 ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
         if (state.canSurvive(world, pos1))
         {
            if (flag1)
            {
               BlockState state1 = state.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
               BlockPos pos2 = pos1.above();
               if (world.getBlockState(pos2).is(Blocks.WATER))
               {
                  world.setBlock(pos1, state, 2);
                  world.setBlock(pos2, state1, 2);
               }
            } else
            {
               world.setBlock(pos1, state, 2);
            }
            flag = true;
         }
      }
      return flag;
   }
}