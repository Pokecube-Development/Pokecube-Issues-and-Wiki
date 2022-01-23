package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import pokecube.legends.blocks.normalblocks.AshLayerBlock;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.WorldgenFeatures;

public class UltraspaceVegetationFeature extends Feature<NetherForestVegetationConfig>
{
   public UltraspaceVegetationFeature(Codec<NetherForestVegetationConfig> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<NetherForestVegetationConfig> context)
   {
      WorldGenLevel world = context.level();
      BlockPos pos = context.origin();
      BlockState stateBelow = world.getBlockState(pos.below());
      NetherForestVegetationConfig vegetationConfig = context.config();
      Random random = context.random();
      if (!stateBelow.is(BlockTags.DIRT))
      {
         return false;
      } else
      {
         int i = pos.getY();
         if (i >= world.getMinBuildHeight() + 1 && i + 1 < world.getMaxBuildHeight())
         {
            int j = 0;

            for(int k = 0; k < vegetationConfig.spreadWidth * vegetationConfig.spreadWidth; ++k)
            {
               BlockPos posOffset = pos.offset(random.nextInt(vegetationConfig.spreadWidth) - random.nextInt(vegetationConfig.spreadWidth),
                       random.nextInt(vegetationConfig.spreadHeight) - random.nextInt(vegetationConfig.spreadHeight),
                       random.nextInt(vegetationConfig.spreadWidth) - random.nextInt(vegetationConfig.spreadWidth));
               
               BlockState stateVegetation = vegetationConfig.stateProvider.getState(random, posOffset);
               
               if (world.isEmptyBlock(posOffset) && posOffset.getY() > world.getMinBuildHeight() && stateVegetation.canSurvive(world, posOffset))
               {
                  world.setBlock(posOffset, stateVegetation, 2);
                  ++j;
               }
            }
            return j > 0;
         } else
         {
            return false;
         }
      }
   }
}