package pokecube.legends.worldgen.features;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.FluidInit;

public class DistorticStoneBouldersFeature extends Feature<ColumnFeatureConfiguration>
{
   private static final ImmutableList<Block> CAN_PLACE_ON =
		   ImmutableList.of(BlockInit.DISTORTIC_GRASS.get(), BlockInit.DISTORTIC_STONE.get(), BlockInit.ULTRA_STONE.get(), FluidInit.DISTORTED_WATER_BLOCK.get());
   private static final int CLUSTERED_REACH = 5;
   private static final int CLUSTERED_SIZE = 50;
   private static final int UNCLUSTERED_REACH = 8;
   private static final int UNCLUSTERED_SIZE = 15;

   public DistorticStoneBouldersFeature(Codec<ColumnFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<ColumnFeatureConfiguration> config)
   {
      int i = config.chunkGenerator().getSeaLevel();
      BlockPos pos = config.origin();
      WorldGenLevel world = config.level();
      Random random = config.random();
      ColumnFeatureConfiguration conlumnConfig = config.config();
      if (!canPlaceAt(world, i, pos.mutable()))
      {
         return false;
      } else
      {
         int j = conlumnConfig.height().sample(random);
         boolean flag = random.nextFloat() < 0.9F;
         int k = Math.min(j, flag ? 5 : 8);
         int l = flag ? 50 : 15;
         boolean flag1 = false;

         for(BlockPos pos1 : BlockPos.randomBetweenClosed(random, l, pos.getX() - k, pos.getY(), pos.getZ() - k, pos.getX() + k, pos.getY(), pos.getZ() + k))
         {
            int i1 = j - pos1.distManhattan(pos);
            if (i1 >= 0)
            {
               flag1 |= this.placeColumn(world, i, pos1, i1, conlumnConfig.reach().sample(random));
            }
         }

         return flag1;
      }
   }

   public boolean placeColumn(LevelAccessor world, int a, BlockPos pos, int height, int reach)
   {
      boolean flag = false;

      for(BlockPos pos1 : BlockPos.betweenClosed(pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX() + reach, pos.getY(), pos.getZ() + reach))
      {
         int i = pos1.distManhattan(pos);
         BlockPos pos2 = isAirOrLavaOcean(world, a, pos1) ? findSurface(world, a, pos1.mutable(), i) : findAir(world, pos1.mutable(), i);
         if (pos2 != null)
         {
            int j = height - i / 2;

            for(BlockPos.MutableBlockPos mutablePos = pos2.mutable(); j >= 0; --j)
            {
               if (isAirOrLavaOcean(world, a, mutablePos))
               {
                  this.setBlock(world, mutablePos, BlockInit.DISTORTIC_STONE.get().defaultBlockState());
                  mutablePos.move(Direction.UP);
                  flag = true;
               } else
               {
                  if (!world.getBlockState(mutablePos).is(BlockInit.DISTORTIC_STONE.get()))
                  {
                     break;
                  }
                  mutablePos.move(Direction.UP);
               }
            }
         }
      }
      return flag;
   }

   @Nullable
   public static BlockPos findSurface(LevelAccessor world, int y, BlockPos.MutableBlockPos pos, int height)
   {
      while(pos.getY() > world.getMinBuildHeight() + 1 && height > 0)
      {
         --height;
         if (canPlaceAt(world, y, pos))
         {
            return pos;
         }
         pos.move(Direction.DOWN);
      }
      return null;
   }

   public static boolean canPlaceAt(LevelAccessor world, int y, BlockPos.MutableBlockPos pos)
   {
      if (!isAirOrLavaOcean(world, y, pos))
      {
         return false;
      } else
      {
         BlockState state = world.getBlockState(pos.move(Direction.DOWN));
         pos.move(Direction.UP);
         return !state.isAir() && CAN_PLACE_ON.contains(state.getBlock());
      }
   }

   @Nullable
   public static BlockPos findAir(LevelAccessor world, BlockPos.MutableBlockPos pos, int height)
   {
      while(pos.getY() < world.getMaxBuildHeight() && height > 0)
      {
         --height;
         BlockState state = world.getBlockState(pos);
         if (!CAN_PLACE_ON.contains(state.getBlock()))
         {
            return null;
         }

         if (state.isAir())
         {
            return pos;
         }
         pos.move(Direction.UP);
      }
      return null;
   }

   public static boolean isAirOrLavaOcean(LevelAccessor world, int height, BlockPos pos)
   {
      BlockState state = world.getBlockState(pos);
      return state.isAir() || state.is(Blocks.LAVA) && pos.getY() <= height;
   }
}