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
import pokecube.legends.init.PlantsInit;

public class TaintedKelpFeature extends Feature<NoneFeatureConfiguration>
{
   public TaintedKelpFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
   {
      int i = 0;
      WorldGenLevel world = context.level();
      BlockPos pos = context.origin();
      Random random = context.random();
      int j = world.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ());
      BlockPos posAbove = new BlockPos(pos.getX(), j, pos.getZ());
      if (world.getBlockState(posAbove).is(Blocks.WATER)) {
         BlockState stateKelp = PlantsInit.TAINTED_KELP.get().defaultBlockState();
         BlockState stateKelpPlant = PlantsInit.TAINTED_KELP_PLANT.get().defaultBlockState();
         int k = 1 + random.nextInt(10);

         for(int l = 0; l <= k; ++l)
         {
            if (world.getBlockState(posAbove).is(Blocks.WATER) && world.getBlockState(posAbove.above()).is(Blocks.WATER) && stateKelpPlant.canSurvive(world, posAbove))
            {
               if (l == k)
               {
                  world.setBlock(posAbove, stateKelp.setValue(KelpBlock.AGE, Integer.valueOf(random.nextInt(4) + 20)), 2);
                  ++i;
               } else
               {
                  world.setBlock(posAbove, stateKelpPlant, 2);
               }
            } else if (l > 0)
            {
               BlockPos pos2 = posAbove.below();
               if (stateKelp.canSurvive(world, pos2) && !world.getBlockState(pos2.below()).is(PlantsInit.TAINTED_KELP.get()))
               {
                  world.setBlock(pos2, stateKelp.setValue(KelpBlock.AGE, Integer.valueOf(random.nextInt(4) + 20)), 2);
                  ++i;
               }
               break;
            }
            posAbove = posAbove.above();
         }
      }
      return i > 0;
   }
}