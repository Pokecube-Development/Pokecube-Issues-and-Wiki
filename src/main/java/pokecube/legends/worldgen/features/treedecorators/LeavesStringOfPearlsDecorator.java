package pokecube.legends.worldgen.features.treedecorators;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import pokecube.legends.blocks.plants.StringOfPearlsBlock;
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
               addHangingVine(world, pos, StringOfPearlsBlock.EAST, blockPos, random);
            }
         }
         if (random.nextInt(4) == 0)
         {
            BlockPos pos1 = listedPos.east();
            if (Feature.isAir(world, pos1))
            {
               addHangingVine(world, pos1, StringOfPearlsBlock.WEST, blockPos, random);
            }
         }
         if (random.nextInt(4) == 0)
         {
            BlockPos pos2 = listedPos.north();
            if (Feature.isAir(world, pos2))
            {
               addHangingVine(world, pos2, StringOfPearlsBlock.SOUTH, blockPos, random);
            }
         }
         if (random.nextInt(4) == 0)
         {
            BlockPos pos3 = listedPos.south();
            if (Feature.isAir(world, pos3))
            {
               addHangingVine(world, pos3, StringOfPearlsBlock.NORTH, blockPos, random);
            }
         }
      });
   }

   public static void addHangingVine(LevelSimulatedReader world, BlockPos pos, BooleanProperty b, BiConsumer<BlockPos, BlockState> blockPos, Random random)
   {
      placeVine(blockPos, pos, b, random);
      int i = 4;

      for(BlockPos pos1 = pos.below(); Feature.isAir(world, pos1) && i > 0; --i)
      {
         placeVine(blockPos, pos1, b, random);
         pos1 = pos1.below();
      }

   }

   public static void placeVine(BiConsumer<BlockPos, BlockState> blockPos, BlockPos pos, BooleanProperty b, Random random)
   {
      blockPos.accept(pos, BlockInit.STRING_OF_PEARLS.get().defaultBlockState()
              .setValue(b, Boolean.valueOf(true))
              .setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)));
   }

   static
   {
      CODEC = Codec.unit(() ->
      {
         return INSTANCE;
      });
   }
}