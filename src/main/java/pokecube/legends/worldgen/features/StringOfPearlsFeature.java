package pokecube.legends.worldgen.features;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import pokecube.legends.init.BlockInit;

public class StringOfPearlsFeature extends Feature<NoneFeatureConfiguration>
{
   public StringOfPearlsFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> config)
   {
      WorldGenLevel world = config.level();
      BlockPos pos = config.origin();
      config.config();
      if (!world.isEmptyBlock(pos))
      {
         return false;
      } else
      {
         for(Direction direction : Direction.values())
         {
            if (direction != Direction.DOWN && VineBlock.isAcceptableNeighbour(world, pos.relative(direction), direction))
            {
               world.setBlock(pos, BlockInit.STRING_OF_PEARLS.get().defaultBlockState()
                    .setValue(VineBlock.getPropertyForFace(direction), Boolean.valueOf(true)), 2);
               return true;
            }
         }
         return false;
      }
   }
}