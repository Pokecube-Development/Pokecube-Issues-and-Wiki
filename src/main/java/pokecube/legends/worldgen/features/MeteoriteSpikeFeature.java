package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;

public class MeteoriteSpikeFeature extends Feature<NoneFeatureConfiguration>
{
   public static final Tag.Named<Block> FEATURES_CANNOT_PLACE_ON = BlockTags.createOptional(new ResourceLocation(Reference.ID, "features_cannot_place_on"));
   
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
      
      BlockState state = world.getBlockState(pos);

      if (world.getBlockState(pos).is(Blocks.AIR) || FEATURES_CANNOT_PLACE_ON.contains(state.getBlock()))
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
                     BlockState stateOffset = world.getBlockState(pos.offset(i1, k, j1));
                     if (stateOffset.isAir() || isDirt(stateOffset) || isSand(stateOffset) || isSandstone(stateOffset) || isStone(stateOffset)
                             || stateOffset.is(BlockInit.ASH_BLOCK.get()) || stateOffset.is(Blocks.ICE) && !FEATURES_CANNOT_PLACE_ON.contains(state.getBlock()))
                     {
                        this.setBlock(world, pos.offset(i1, k, j1), BlockInit.METEORITE_BLOCK.get().defaultBlockState());
                     }

                     if (k != 0 && l > 1)
                     {
                        stateOffset = world.getBlockState(pos.offset(i1, -k, j1));
                        if (stateOffset.isAir() || isDirt(stateOffset) || isSand(stateOffset) || isSandstone(stateOffset) || isStone(stateOffset)
                                || stateOffset.is(BlockInit.ASH_BLOCK.get()) || stateOffset.is(Blocks.ICE) && !FEATURES_CANNOT_PLACE_ON.contains(state.getBlock()))
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

   public static boolean isSandstone(BlockState state)
   {
      return state.is(Tags.Blocks.SANDSTONE);
   }
}