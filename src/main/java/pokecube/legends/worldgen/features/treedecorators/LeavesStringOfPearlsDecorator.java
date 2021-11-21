package pokecube.legends.worldgen.features.treedecorators;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.WorldgenFeatures;

public class LeavesStringOfPearlsDecorator extends TreeDecorator
{
   public static final Codec<LeavesStringOfPearlsDecorator> CODEC;
   public static final LeavesStringOfPearlsDecorator INSTANCE = new LeavesStringOfPearlsDecorator();

   public TreeDecoratorType<?> type()
   {
      return WorldgenFeatures.LEAVES_STRING_OF_PEARLS.get();
   }

   @Override
   public void place(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> blockPos, Random random, List<BlockPos> listPos, List<BlockPos> listPos1)
   {
      listPos1.forEach((listedPos) ->
      {
         if (random.nextInt(4) == 0)
         {
            BlockPos pos = listedPos.west();
            if (Feature.isAir(world, pos))
            {
               addHangingVine(world, pos, VineBlock.EAST, blockPos);
            }
         }
         if (random.nextInt(4) == 0)
         {
            BlockPos pos1 = listedPos.east();
            if (Feature.isAir(world, pos1))
            {
               addHangingVine(world, pos1, VineBlock.WEST, blockPos);
            }
         }
         if (random.nextInt(4) == 0)
         {
            BlockPos pos2 = listedPos.north();
            if (Feature.isAir(world, pos2))
            {
               addHangingVine(world, pos2, VineBlock.SOUTH, blockPos);
            }
         }
         if (random.nextInt(4) == 0)
         {
            BlockPos pos3 = listedPos.south();
            if (Feature.isAir(world, pos3))
            {
               addHangingVine(world, pos3, VineBlock.NORTH, blockPos);
            }
         }
      });
   }

   public static void addHangingVine(LevelSimulatedReader world, BlockPos pos, BooleanProperty b, BiConsumer<BlockPos, BlockState> blockPos)
   {
      placeVine(blockPos, pos, b);
      int i = 4;

      for(BlockPos pos1 = pos.below(); Feature.isAir(world, pos1) && i > 0; --i)
      {
         placeVine(blockPos, pos1, b);
         pos1 = pos1.below();
      }

   }

   public static void placeVine(BiConsumer<BlockPos, BlockState> blockPos, BlockPos pos, BooleanProperty b)
   {
      blockPos.accept(pos, BlockInit.STRING_OF_PEARLS.get().defaultBlockState().setValue(b, Boolean.valueOf(true)));
   }

   static
   {
      CODEC = Codec.unit(() ->
      {
         return INSTANCE;
      });
   }
}