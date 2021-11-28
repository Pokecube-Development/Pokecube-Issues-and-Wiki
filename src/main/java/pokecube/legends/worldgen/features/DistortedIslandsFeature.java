package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import pokecube.legends.init.BlockInit;

public class DistortedIslandsFeature extends Feature<NoneFeatureConfiguration>
{
   public DistortedIslandsFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
   {
      WorldGenLevel worldLevel = context.level();
      Random random = context.random();
      BlockPos pos = context.origin();
      float f = (float)(random.nextInt(4) + 4);

      for(int y = 0 + random.nextInt(2); f > 0.5F; --y)
      {
         for(int x = Mth.floor(-f); x <= Mth.ceil(f); ++x)
         {
            for(int z = Mth.floor(-f); z <= Mth.ceil(f); ++z)
            {
                if ((float)(x * x + z * z) <= (f + 1.0F) * (f + 1.0F))
                {
                    this.setBlock(worldLevel, pos.offset(x, y, z), BlockInit.DISTORTIC_STONE.get().defaultBlockState());
                     this.setBlock(worldLevel, pos.offset(x, y + 1, z), BlockInit.DISTORTIC_GRASS.get().defaultBlockState());
                     this.setBlock(worldLevel, pos.offset(x, y - 1, z), BlockInit.CRACKED_DISTORTIC_STONE.get().defaultBlockState()
                         .setValue(DirectionalBlock.FACING, Direction.DOWN));
                     this.setBlock(worldLevel, pos.offset(x, y - 2, z), BlockInit.DISTORTIC_GLOWSTONE.get().defaultBlockState());
                }
            }
         }
         f = (float)((double)f - ((double)random.nextInt(2) + 0.5D));
      }
      return true;
   }
}