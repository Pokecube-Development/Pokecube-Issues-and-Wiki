package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import pokecube.legends.init.PlantsInit;

public class TaintedSeagrassFeature extends Feature<ProbabilityFeatureConfiguration>
{
   public TaintedSeagrassFeature(final Codec<ProbabilityFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(final FeaturePlaceContext<ProbabilityFeatureConfiguration> context)
   {
      boolean flag = false;
      final Random random = context.random();
      final WorldGenLevel world = context.level();
      final BlockPos pos = context.origin();
      final ProbabilityFeatureConfiguration probConfig = context.config();
      final int i = random.nextInt(8) - random.nextInt(8);
      final int j = random.nextInt(8) - random.nextInt(8);
      final int k = world.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX() + i, pos.getZ() + j);
      final BlockPos pos1 = new BlockPos(pos.getX() + i, k, pos.getZ() + j);
      if (world.getBlockState(pos1).is(Blocks.WATER))
      {
         final boolean flag1 = random.nextDouble() < probConfig.probability;
         final BlockState state = flag1 ? PlantsInit.TALL_TAINTED_SEAGRASS.get().defaultBlockState() : PlantsInit.TAINTED_SEAGRASS.get().defaultBlockState();
         if (state.canSurvive(world, pos1))
         {
            if (flag1)
            {
               final BlockState state1 = state.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
               final BlockPos pos2 = pos1.above();
               if (world.getBlockState(pos2).is(Blocks.WATER))
               {
                  world.setBlock(pos1, state, 2);
                  world.setBlock(pos2, state1, 2);
               }
            }
            else world.setBlock(pos1, state, 2);
            flag = true;
         }
      }
      return flag;
   }
}