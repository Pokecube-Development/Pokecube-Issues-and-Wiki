package pokecube.legends.worldgen.features;

import java.util.Random;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TaintedKelpFeature extends Feature<NoneFeatureConfiguration>
{
   public TaintedKelpFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> config)
   {
      int i = 0;
      WorldGenLevel world = config.level();
      BlockPos pos = config.origin();
      Random random = config.random();
      int j = world.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ());
      BlockPos pos1 = new BlockPos(pos.getX(), j, pos.getZ());
      if (world.getBlockState(pos1).is(Blocks.WATER)) {
         BlockState state = Blocks.KELP.defaultBlockState();
         BlockState state1 = Blocks.KELP_PLANT.defaultBlockState();
         int k = 1 + random.nextInt(10);

         for(int l = 0; l <= k; ++l)
         {
            if (world.getBlockState(pos1).is(Blocks.WATER) && world.getBlockState(pos1.above()).is(Blocks.WATER) && state1.canSurvive(world, pos1))
            {
               if (l == k)
               {
                  world.setBlock(pos1, state.setValue(KelpBlock.AGE, Integer.valueOf(random.nextInt(4) + 20)), 2);
                  ++i;
               } else
               {
                  world.setBlock(pos1, state1, 2);
               }
            } else if (l > 0)
            {
               BlockPos pos2 = pos1.below();
               if (state.canSurvive(world, pos2) && !world.getBlockState(pos2.below()).is(Blocks.KELP))
               {
                  world.setBlock(pos2, state.setValue(KelpBlock.AGE, Integer.valueOf(random.nextInt(4) + 20)), 2);
                  ++i;
               }
               break;
            }
            pos1 = pos1.above();
         }
      }
      return i > 0;
   }
}