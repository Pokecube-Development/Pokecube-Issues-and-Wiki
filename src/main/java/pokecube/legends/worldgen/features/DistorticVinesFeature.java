package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;

public class DistorticVinesFeature extends Feature<NoneFeatureConfiguration>
{
   public DistorticVinesFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
   {
      return place(context.level(), context.random(), context.origin(), 8, 4, 8);
   }

   public static boolean place(LevelAccessor world, Random random, BlockPos pos, int x, int y, int z)
   {
      if (isInvalidPlacementLocation(world, pos))
      {
         return false;
      } else
      {
         placeDistorticVines(world, random, pos, x, y, z);
         return true;
      }
   }

   public static void placeDistorticVines(LevelAccessor world, Random random, BlockPos pos, int x, int y, int z)
   {
      BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < x * x; ++i)
      {
         mutablePos.set(pos).move(Mth.nextInt(random, -x, x), Mth.nextInt(random, -y, y), Mth.nextInt(random, -x, x));
         if (findFirstAirBlockAboveGround(world, mutablePos) && !isInvalidPlacementLocation(world, mutablePos))
         {
            int j = Mth.nextInt(random, 1, z);
            if (random.nextInt(6) == 0)
            {
               j *= 2;
            }
            if (random.nextInt(5) == 0)
            {
               j = 1;
            }
            int k = 17;
            int l = 25;
            placeDistorticVinesColumn(world, random, mutablePos, j, 17, 25);
         }
      }
   }

   public static boolean findFirstAirBlockAboveGround(LevelAccessor world, BlockPos.MutableBlockPos pos)
   {
      do
      {
         pos.move(0, -1, 0);
         if (world.isOutsideBuildHeight(pos))
         {
            return false;
         }
      } while(world.getBlockState(pos).isAir());
      pos.move(0, 1, 0);
      return true;
   }

   public static void placeDistorticVinesColumn(LevelAccessor world, Random random, BlockPos.MutableBlockPos pos, int x, int y, int z)
   {
      for(int i = 1; i <= x; ++i)
      {
         if (world.isEmptyBlock(pos))
         {
            if (i == x || !world.isEmptyBlock(pos.above()))
            {
               world.setBlock(pos, PlantsInit.DISTORTIC_VINES.get().defaultBlockState()
                       .setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(random, y, z))), 2);
               break;
            }

            world.setBlock(pos, PlantsInit.DISTORTIC_VINES_PLANT.get().defaultBlockState(), 2);
         }
         pos.move(Direction.UP);
      }

   }

   public static boolean isInvalidPlacementLocation(LevelAccessor world, BlockPos pos)
   {
      if (!world.isEmptyBlock(pos))
      {
         return true;
      } else
      {
         BlockState state = world.getBlockState(pos.below());
         return !state.is(BlockInit.DISTORTIC_STONE.get()) && !state.is(BlockInit.DISTORTIC_GRASS.get()) && !state.is(BlockInit.DISTORTIC_MIRROR.get());
      }
   }
}