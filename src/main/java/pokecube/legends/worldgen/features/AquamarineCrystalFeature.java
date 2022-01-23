package pokecube.legends.worldgen.features;

import java.util.Optional;
import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import pokecube.legends.Reference;
import pokecube.legends.worldgen.utils.AquamarineUtils;

public class AquamarineCrystalFeature extends Feature<PointedDripstoneConfiguration>
{
   // Block Tag
   public static final Tag.Named<Block> BASE_STONE_ULTRASPACE = BlockTags.createOptional(new ResourceLocation(Reference.ID, "base_stone_ultraspace"));
   
   public AquamarineCrystalFeature(final Codec<PointedDripstoneConfiguration> config)
   {
      super(config);
   }

   public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> context)
   {
      LevelAccessor world = context.level();
      BlockPos pos = context.origin();
      Random random = context.random();
      PointedDripstoneConfiguration aquamarineCrystalConfig = context.config();
      Optional<Direction> optional = getTipDirection(world, pos, random);
      if (optional.isEmpty())
      {
         return false;
      } else
      {
         BlockPos pos1 = pos.relative(optional.get().getOpposite());
         createPatchOfAquamarineBlocks(world, random, pos1, aquamarineCrystalConfig);
         int i = random.nextFloat() < aquamarineCrystalConfig.chanceOfTallerDripstone
                 && AquamarineUtils.isEmptyOrWater(world.getBlockState(pos.relative(optional.get()))) ? 2 : 1;
         AquamarineUtils.growAquamarineCrystal(world, pos, optional.get(), i, false);
         return true;
      }
   }

   public static Optional<Direction> getTipDirection(LevelAccessor world, BlockPos pos, Random random)
   {
      boolean flag = AquamarineUtils.isAquamarineBase(world.getBlockState(pos.above()));
      boolean flag1 = AquamarineUtils.isAquamarineBase(world.getBlockState(pos.below()));
      if (flag && flag1)
      {
         return Optional.of(random.nextBoolean() ? Direction.DOWN : Direction.UP);
      } else if (flag)
      {
         return Optional.of(Direction.DOWN);
      } else
      {
         return flag1 ? Optional.of(Direction.UP) : Optional.empty();
      }
   }

   public static void createPatchOfAquamarineBlocks(LevelAccessor world, Random random, BlockPos pos, PointedDripstoneConfiguration config)
   {
       AquamarineUtils.placeAquamarineBlockIfPossible(world, pos);

      for(Direction direction : Direction.Plane.HORIZONTAL)
      {
         if (!(random.nextFloat() > config.chanceOfDirectionalSpread))
         {
            BlockPos pos1 = pos.relative(direction);
            AquamarineUtils.placeAquamarineBlockIfPossible(world, pos1);
            if (!(random.nextFloat() > config.chanceOfSpreadRadius2))
            {
               BlockPos pos2 = pos1.relative(Direction.getRandom(random));
               AquamarineUtils.placeAquamarineBlockIfPossible(world, pos2);
               if (!(random.nextFloat() > config.chanceOfSpreadRadius3))
               {
                  BlockPos pos3 = pos2.relative(Direction.getRandom(random));
                  AquamarineUtils.placeAquamarineBlockIfPossible(world, pos3);
               }
            }
         }
      }
   }
}