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

public class TrunkStringOfPearlsDecorator extends TreeDecorator
{
   public static final Codec<TrunkStringOfPearlsDecorator> CODEC;
   public static final TrunkStringOfPearlsDecorator INSTANCE = new TrunkStringOfPearlsDecorator();

   protected TreeDecoratorType<?> type()
   {
          return WorldgenFeatures.TRUNK_STRING_OF_PEARLS.get();
   }

   @Override
   public void place(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> blockPos, Random random, List<BlockPos> listPos, List<BlockPos> listPos1)
   {
      listPos.forEach((listedPos) ->
      {
         if (random.nextInt(3) > 0)
         {
            BlockPos pos = listedPos.west();
            if (Feature.isAir(world, pos))
            {
               placeVine(blockPos, pos, VineBlock.EAST);
            }
         }

         if (random.nextInt(3) > 0)
         {
            BlockPos pos1 = listedPos.east();
            if (Feature.isAir(world, pos1))
            {
               placeVine(blockPos, pos1, VineBlock.WEST);
            }
         }

         if (random.nextInt(3) > 0)
         {
            BlockPos pos2 = listedPos.north();
            if (Feature.isAir(world, pos2))
            {
               placeVine(blockPos, pos2, VineBlock.SOUTH);
            }
         }

         if (random.nextInt(3) > 0)
         {
            BlockPos pos3 = listedPos.south();
            if (Feature.isAir(world, pos3))
            {
               placeVine(blockPos, pos3, VineBlock.NORTH);
            }
         }

      });
   }

   public static void placeVine(BiConsumer<BlockPos, BlockState> blockPos, BlockPos pos, BooleanProperty b)
   {
      blockPos.accept(pos, BlockInit.STRING_OF_PEARLS.get().defaultBlockState().setValue(b, Boolean.valueOf(true)));
   }

   static {
      CODEC = Codec.unit(() ->
      {
         return INSTANCE;
      });
   }
}