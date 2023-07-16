package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraftforge.common.Tags;

public class RockFeature extends Feature<BlockStateConfiguration>
{
   public RockFeature(Codec<BlockStateConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<BlockStateConfiguration> context)
   {
      BlockPos pos = context.origin();
      WorldGenLevel world = context.level();
      Random random = context.random();

      BlockStateConfiguration stateConfig;
      for(stateConfig = context.config(); pos.getY() > world.getMinBuildHeight() + 3; pos = pos.below())
      {
         BlockState state = world.getBlockState(pos.below());
         if (!world.isEmptyBlock(pos.below()))
         {
            if (isSand(state) || isSandstone(state) || isDirt(state) || isStone(state))
            {
               break;
            }
         }
      }

      if (pos.getY() <= world.getMinBuildHeight() + 3)
      {
         return false;
      } else
      {
         for(int l = 0; l < 3; ++l)
         {
            int i = random.nextInt(2);
            int j = random.nextInt(2);
            int k = random.nextInt(2);
            float f = (float)(i + j + k) * 0.333F + 0.5F;

            for(BlockPos pos1 : BlockPos.betweenClosed(pos.offset(-i, -j, -k), pos.offset(i, j, k)))
            {
               if (pos1.distSqr(pos) <= (double)(f * f))
               {
                  world.setBlock(pos1, stateConfig.state, 4);
               }
            }
            pos = pos.offset(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
         }
         return true;
      }
   }

   public static boolean isSandstone(BlockState state)
   {
      return state.is(Tags.Blocks.SANDSTONE);
   }

   public static boolean isSand(BlockState state)
   {
      return state.is(Tags.Blocks.SAND) || state.is(BlockTags.SAND);
   }
}