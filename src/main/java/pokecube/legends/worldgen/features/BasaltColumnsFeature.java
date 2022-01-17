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

public class BasaltColumnsFeature extends Feature<ColumnFeatureConfiguration>
{
   public static final ImmutableList<Block> CANNOT_PLACE_ON =
		   ImmutableList.of(BlockInit.AGED_LEAVES.get(), BlockInit.ASH.get(), BlockInit.CORRUPTED_LEAVES.get(), BlockInit.DISTORTIC_LEAVES.get(),
				   BlockInit.DYNA_LEAVES_PASTEL_PINK.get(), BlockInit.DYNA_LEAVES_PINK.get(), BlockInit.DYNA_LEAVES_RED.get(),
				   BlockInit.INVERTED_LEAVES.get(), BlockInit.MIRAGE_LEAVES.get(), BlockInit.TEMPORAL_LEAVES.get(),
				   Blocks.BEDROCK, Blocks.CHEST, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS,
				   Blocks.NETHER_WART, Blocks.SNOW, Blocks.SPAWNER);

   private static final int CLUSTERED_REACH = 5;
   private static final int CLUSTERED_SIZE = 50;
   private static final int UNCLUSTERED_REACH = 8;
   private static final int UNCLUSTERED_SIZE = 15;

   public BasaltColumnsFeature(final Codec<ColumnFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(final FeaturePlaceContext<ColumnFeatureConfiguration> context)
   {
      final int i = context.chunkGenerator().getSeaLevel();
      final BlockPos pos = context.origin();
      final WorldGenLevel world = context.level();
      final Random random = context.random();
      final ColumnFeatureConfiguration conlumnConfig = context.config();
      if (!BasaltColumnsFeature.canPlaceAt(world, i, pos.mutable())) return false;
    else
      {
         final int j = conlumnConfig.height().sample(random);
         final boolean flag = random.nextFloat() < 0.9F;
         final int k = Math.min(j, flag ? BasaltColumnsFeature.CLUSTERED_REACH : BasaltColumnsFeature.UNCLUSTERED_REACH);
         final int l = flag ? BasaltColumnsFeature.CLUSTERED_SIZE : BasaltColumnsFeature.UNCLUSTERED_SIZE;
         boolean flag1 = false;

         for(final BlockPos pos1 : BlockPos.randomBetweenClosed(random, l, pos.getX() - k, pos.getY(), pos.getZ() - k, pos.getX() + k, pos.getY(), pos.getZ() + k))
         {
            final int i1 = j - pos1.distManhattan(pos);
            if (i1 >= 0) flag1 |= this.placeColumn(world, i, pos1, i1, conlumnConfig.reach().sample(random));
         }

         return flag1;
      }
   }

   public boolean placeColumn(final LevelAccessor world, final int a, final BlockPos pos, final int height, final int reach)
   {
      boolean flag = false;

      for(final BlockPos pos1 : BlockPos.betweenClosed(pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX() + reach, pos.getY(), pos.getZ() + reach))
      {
         final int i = pos1.distManhattan(pos);
         final BlockPos pos2 = BasaltColumnsFeature.isAirOrLavaOcean(world, a, pos1) ? BasaltColumnsFeature.findSurface(world, a, pos1.mutable(), i) : BasaltColumnsFeature.findAir(world, pos1.mutable(), i);
         if (pos2 != null)
         {
            int j = height - i / 2;

            for(final BlockPos.MutableBlockPos mutablePos = pos2.mutable(); j >= 0; --j)
                if (BasaltColumnsFeature.isAirOrLavaOcean(world, a, mutablePos))
                   {
                      this.setBlock(world, mutablePos, Blocks.BASALT.defaultBlockState());
                      mutablePos.move(Direction.UP);
                      flag = true;
                   } else
                   {
                      if (!world.getBlockState(mutablePos).is(Blocks.BASALT)) break;
                      mutablePos.move(Direction.UP);
                   }
         }
      }
      return flag;
   }

   @Nullable
   public static BlockPos findSurface(final LevelAccessor world, final int y, final BlockPos.MutableBlockPos pos, int height)
   {
      while(pos.getY() > world.getMinBuildHeight() + 1 && height > 0)
      {
         --height;
         if (BasaltColumnsFeature.canPlaceAt(world, y, pos)) return pos;
         pos.move(Direction.DOWN);
      }
      return null;
   }

   public static boolean canPlaceAt(final LevelAccessor world, final int y, final BlockPos.MutableBlockPos pos)
   {
      if (!BasaltColumnsFeature.isAirOrLavaOcean(world, y, pos)) return false;
    else
      {
         final BlockState state = world.getBlockState(pos.move(Direction.DOWN));
         pos.move(Direction.UP);
         return !state.isAir() && !BasaltColumnsFeature.CANNOT_PLACE_ON.contains(state.getBlock());
      }
   }

   @Nullable
   public static BlockPos findAir(final LevelAccessor world, final BlockPos.MutableBlockPos pos, int height)
   {
      while(pos.getY() < world.getMaxBuildHeight() && height > 0)
      {
         --height;
         final BlockState state = world.getBlockState(pos);
         if (BasaltColumnsFeature.CANNOT_PLACE_ON.contains(state.getBlock())) return null;

         if (state.isAir()) return pos;
         pos.move(Direction.UP);
      }
      return null;
   }

   public static boolean isAirOrLavaOcean(final LevelAccessor world, final int height, final BlockPos pos)
   {
      final BlockState state = world.getBlockState(pos);
      return state.isAir() || state.is(Blocks.LAVA) && pos.getY() <= height;
   }
}