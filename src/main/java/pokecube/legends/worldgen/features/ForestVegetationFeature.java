package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class ForestVegetationFeature extends Feature<BlockPileConfiguration>
{
   public ForestVegetationFeature(Codec<BlockPileConfiguration> config)
   {
      super(config);
   }

   public boolean place(FeaturePlaceContext<BlockPileConfiguration> context)
   {
      return place(context.level(), context.random(), context.origin(), context.config(), 8, 4);
   }

   public static boolean place(LevelAccessor world, Random random, BlockPos pos, BlockPileConfiguration config, int x, int y)
   {
      BlockState state = world.getBlockState(pos.below());
      if (!state.is(BlockTags.DIRT))
      {
         return false;
      } else
      {
         int i = pos.getY();
         if (i >= world.getMinBuildHeight() + 1 && i + 1 < world.getMaxBuildHeight())
         {
            int j = 0;

            for(int k = 0; k < x * x; ++k)
            {
               BlockPos pos1 = pos.offset(random.nextInt(x) - random.nextInt(x), random.nextInt(y) - random.nextInt(y), random.nextInt(x) - random.nextInt(x));
               BlockState state1 = config.stateProvider.getState(random, pos1);
               if (world.isEmptyBlock(pos1) && pos1.getY() > world.getMinBuildHeight() && state1.canSurvive(world, pos1))
               {
                  world.setBlock(pos1, state1, 2);
                  ++j;
               }
            }

            return j > 0;
         } else
         {
            return false;
         }
      }
   }
}