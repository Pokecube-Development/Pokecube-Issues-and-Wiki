package pokecube.legends.worldgen.features;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DeadCoralTreeFeature extends DeadCoralFeature
{
   public DeadCoralTreeFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   public boolean placeFeature(LevelAccessor world, Random random, BlockPos pos, BlockState state)
   {
      BlockPos.MutableBlockPos mutablePos = pos.mutable();
      int i = random.nextInt(3) + 1;

      for(int j = 0; j < i; ++j)
      {
         if (!this.placeDeadCoralBlock(world, random, mutablePos, state))
         {
            return true;
         }
         mutablePos.move(Direction.UP);
      }

      BlockPos pos1 = mutablePos.immutable();
      int k = random.nextInt(3) + 2;
      List<Direction> list = Lists.newArrayList(Direction.Plane.HORIZONTAL);
      Collections.shuffle(list, random);

      for(Direction direction : list.subList(0, k))
      {
         mutablePos.set(pos1);
         mutablePos.move(direction);
         int l = random.nextInt(5) + 2;
         int i1 = 0;

         for(int j1 = 0; j1 < l && this.placeDeadCoralBlock(world, random, mutablePos, state); ++j1)
         {
            ++i1;
            mutablePos.move(Direction.UP);
            if (j1 == 0 || i1 >= 2 && random.nextFloat() < 0.25F)
            {
               mutablePos.move(direction);
               i1 = 0;
            }
         }
      }
      return true;
   }
}