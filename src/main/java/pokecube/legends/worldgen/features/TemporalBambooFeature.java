package pokecube.legends.worldgen.features;

import java.util.Random;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import pokecube.legends.blocks.plants.TemporalBambooBlock;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;

public class TemporalBambooFeature extends Feature<ProbabilityFeatureConfiguration>
{
   private static final BlockState BAMBOO_TRUNK = PlantsInit.TEMPORAL_BAMBOO.get().defaultBlockState()
           .setValue(TemporalBambooBlock.AGE, Integer.valueOf(1)).setValue(TemporalBambooBlock.LEAVES, BambooLeaves.NONE)
           .setValue(TemporalBambooBlock.STAGE, Integer.valueOf(0));
   private static final BlockState BAMBOO_FINAL_LARGE = BAMBOO_TRUNK.setValue(TemporalBambooBlock.LEAVES, BambooLeaves.LARGE)
           .setValue(TemporalBambooBlock.STAGE, Integer.valueOf(1));
   private static final BlockState BAMBOO_TOP_LARGE = BAMBOO_TRUNK.setValue(TemporalBambooBlock.LEAVES, BambooLeaves.LARGE);
   private static final BlockState BAMBOO_TOP_SMALL = BAMBOO_TRUNK.setValue(TemporalBambooBlock.LEAVES, BambooLeaves.SMALL);

   public TemporalBambooFeature(Codec<ProbabilityFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context)
   {
      int i = 0;
      BlockPos pos = context.origin();
      WorldGenLevel world = context.level();
      Random random = context.random();
      ProbabilityFeatureConfiguration probConfig = context.config();
      BlockPos.MutableBlockPos mutablePos = pos.mutable();
      BlockPos.MutableBlockPos mutablePos1 = pos.mutable();
      if (world.isEmptyBlock(mutablePos)) {
         if (PlantsInit.TEMPORAL_BAMBOO.get().defaultBlockState().canSurvive(world, mutablePos))
         {
            int j = random.nextInt(12) + 5;
            if (random.nextFloat() < probConfig.probability)
            {
               int k = random.nextInt(4) + 1;

               for(int l = pos.getX() - k; l <= pos.getX() + k; ++l)
               {
                  for(int i1 = pos.getZ() - k; i1 <= pos.getZ() + k; ++i1)
                  {
                     int j1 = l - pos.getX();
                     int k1 = i1 - pos.getZ();
                     if (j1 * j1 + k1 * k1 <= k * k) {
                        mutablePos1.set(l, world.getHeight(Heightmap.Types.WORLD_SURFACE, l, i1) - 1, i1);
                        if (isDirt(world.getBlockState(mutablePos1))) {
                           world.setBlock(mutablePos1, BlockInit.JUNGLE_GRASS.get().defaultBlockState(), 2);
                        }
                     }
                  }
               }
            }
            for(int l1 = 0; l1 < j && world.isEmptyBlock(mutablePos); ++l1)
            {
               world.setBlock(mutablePos, BAMBOO_TRUNK, 2);
               mutablePos.move(Direction.UP, 1);
            }
            if (mutablePos.getY() - pos.getY() >= 3)
            {
               world.setBlock(mutablePos, BAMBOO_FINAL_LARGE, 2);
               world.setBlock(mutablePos.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
               world.setBlock(mutablePos.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
            }
         }
         ++i;
      }
      return i > 0;
   }
}