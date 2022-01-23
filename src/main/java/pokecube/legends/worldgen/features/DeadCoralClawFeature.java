package pokecube.legends.worldgen.features;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DeadCoralClawFeature extends DeadCoralFeature
{
   public DeadCoralClawFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   public boolean placeFeature(LevelAccessor world, Random random, BlockPos pos, BlockState state)
   {
      if (!this.placeDeadCoralBlock(world, random, pos, state))
      {
         return false;
      } else
      {
         Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
         int i = random.nextInt(2) + 2;
         List<Direction> list = Lists.newArrayList(direction, direction.getClockWise(), direction.getCounterClockWise());
         Collections.shuffle(list, random);

         for(Direction direction1 : list.subList(0, i))
         {
            BlockPos.MutableBlockPos mutablePos = pos.mutable();
            int j = random.nextInt(2) + 1;
            mutablePos.move(direction1);
            int k;
            Direction direction2;
            if (direction1 == direction)
            {
               direction2 = direction;
               k = random.nextInt(3) + 2;
            } else
            {
               mutablePos.move(Direction.UP);
               Direction[] adirection = new Direction[]{direction1, Direction.UP};
               direction2 = Util.getRandom(adirection, random);
               k = random.nextInt(3) + 3;
            }

            for(int l = 0; l < j && this.placeDeadCoralBlock(world, random, mutablePos, state); ++l)
            {
               mutablePos.move(direction2);
            }
            mutablePos.move(direction2.getOpposite());
            mutablePos.move(Direction.UP);

            for(int i1 = 0; i1 < k; ++i1)
            {
               mutablePos.move(direction);
               if (!this.placeDeadCoralBlock(world, random, mutablePos, state))
               {
                  break;
               }
               if (random.nextFloat() < 0.25F)
               {
                  mutablePos.move(Direction.UP);
               }
            }
         }
         return true;
      }
   }
}