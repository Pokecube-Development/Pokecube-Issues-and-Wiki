package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.Tags;
import pokecube.legends.blocks.normalblocks.AshLayerBlock;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.WorldgenFeatures;

public class MeteoriteSpikeFeature extends Feature<NoneFeatureConfiguration>
{
   public MeteoriteSpikeFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
   {
      BlockPos pos = context.origin();
      Random random = context.random();

      WorldGenLevel world;
      for(world = context.level(); world.isEmptyBlock(pos) && pos.getY() > world.getMinBuildHeight() + 2; pos = pos.below())
      {}

      if (!world.getBlockState(pos).is(BlockInit.BLACKENED_SAND.get()) && !world.getBlockState(pos).is(BlockInit.BLACKENED_SANDSTONE.get()))
      {
         return false;
      } else
      {
         pos = pos.above(random.nextInt(4));
         int i = random.nextInt(4) + 7;
         int j = i / 4 + random.nextInt(2);
         if (j > 1 && random.nextInt(60) == 0)
         {
            pos = pos.above(10 + random.nextInt(30));
         }

         for(int k = 0; k < i; ++k)
         {
            float f = (1.0F - (float)k / (float)i) * (float)j;
            int l = Mth.ceil(f);

            for(int i1 = -l; i1 <= l; ++i1)
            {
               float f1 = (float)Mth.abs(i1) - 0.25F;

               for(int j1 = -l; j1 <= l; ++j1)
               {
                  float f2 = (float)Mth.abs(j1) - 0.25F;
                  if ((i1 == 0 && j1 == 0 || !(f1 * f1 + f2 * f2 > f * f)) && (i1 != -l && i1 != l && j1 != -l && j1 != l || !(random.nextFloat() > 0.75F)))
                  {
                     BlockState state = world.getBlockState(pos.offset(i1, k, j1));
                     if (state.isAir() || isDirt(state) || isSand(state) || state.is(BlockInit.ASH.get()) || state.is(BlockInit.ASH_BLOCK.get()) || state.is(Blocks.ICE))
                     {
                        this.setBlock(world, pos.offset(i1, k, j1), BlockInit.METEORITE_BLOCK.get().defaultBlockState());
                     }

                     if (k != 0 && l > 1)
                     {
                        state = world.getBlockState(pos.offset(i1, -k, j1));
                        if (state.isAir() || isDirt(state) || isSand(state) || state.is(BlockInit.ASH.get()) || state.is(BlockInit.ASH_BLOCK.get()) || state.is(Blocks.ICE))
                        {
                           this.setBlock(world, pos.offset(i1, -k, j1), BlockInit.METEORITE_BLOCK.get().defaultBlockState());
                        }
                     }
                  }
               }
            }
         }

         int k1 = j - 1;
         if (k1 < 0)
         {
            k1 = 0;
         } else if (k1 > 1)
         {
            k1 = 1;
         }

         for(int l1 = -k1; l1 <= k1; ++l1)
         {
            for(int i2 = -k1; i2 <= k1; ++i2)
            {
               BlockPos posOffset = pos.offset(l1, -1, i2);
               int j2 = 50;
               if (Math.abs(l1) == 1 && Math.abs(i2) == 1)
               {
                  j2 = random.nextInt(5);
               }

               while(posOffset.getY() > 50)
               {
                  BlockState stateOffset = world.getBlockState(posOffset);
                  if (!stateOffset.isAir() && !isDirt(stateOffset) && !isSand(stateOffset) && !stateOffset.is(BlockInit.ASH.get()) && !stateOffset.is(BlockInit.ASH_BLOCK.get())
                          && !stateOffset.is(Blocks.ICE) && !stateOffset.is(BlockInit.METEORITE_BLOCK.get()))
                  {
                     break;
                  }

                  this.setBlock(world, posOffset, BlockInit.METEORITE_BLOCK.get().defaultBlockState());
                  posOffset = posOffset.below();
                  --j2;
                  if (j2 <= 0)
                  {
                     posOffset = posOffset.below(random.nextInt(5) + 1);
                     j2 = random.nextInt(5);
                  }
               }
            }
         }

         return true;
      }
   }

   public static boolean isSand(BlockState state)
   {
      return state.is(BlockTags.SAND) || state.is(Tags.Blocks.SAND);
   }
}