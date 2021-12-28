package pokecube.legends.worldgen.features;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.utils.AquamarineUtils;

public class LargeUnrefinedAquamarine extends Feature<LargeDripstoneConfiguration>
{
   // Block Tag
   public static final Tag.Named<Block> BASE_STONE_ULTRASPACE = BlockTags.createOptional(new ResourceLocation(Reference.ID, "base_stone_ultraspace"));
   
   public LargeUnrefinedAquamarine(final Codec<LargeDripstoneConfiguration> config)
   {
      super(config);
   }

   public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> context)
   {
      WorldGenLevel world = context.level();
      BlockPos pos = context.origin();
      LargeDripstoneConfiguration largeUnrefinedAquamarineConfig = context.config();
      Random random = context.random();
      if (!AquamarineUtils.isEmptyOrWater(world, pos))
      {
         return false;
      } else {
         Optional<Column> optional = Column.scan(world, pos, largeUnrefinedAquamarineConfig.floorToCeilingSearchRange, 
                 AquamarineUtils::isEmptyOrWater, AquamarineUtils::isAquamarineBaseOrLava);
         if (optional.isPresent() && optional.get() instanceof Column.Range)
         {
            Column.Range column = (Column.Range)optional.get();
            if (column.height() < 4)
            {
               return false;
            } else
            {
               int i = (int)((float)column.height() * largeUnrefinedAquamarineConfig.maxColumnRadiusToCaveHeightRatio);
               int j = Mth.clamp(i, largeUnrefinedAquamarineConfig.columnRadius.getMinValue(), 
                       largeUnrefinedAquamarineConfig.columnRadius.getMaxValue());
               int k = Mth.randomBetweenInclusive(random, largeUnrefinedAquamarineConfig.columnRadius.getMinValue(), j);
               LargeUnrefinedAquamarine.LargeUnrefineAquamarine largeUnrefinedAquamarine = makeAquamarine(pos.atY(column.ceiling() - 1), 
                       false, random, k, largeUnrefinedAquamarineConfig.stalactiteBluntness,
                       largeUnrefinedAquamarineConfig.heightScale);
               
               LargeUnrefinedAquamarine.LargeUnrefineAquamarine largeUnrefinedAquamarine1 = makeAquamarine(pos.atY(column.floor() + 1), 
                       true, random, k, largeUnrefinedAquamarineConfig.stalagmiteBluntness,
                       largeUnrefinedAquamarineConfig.heightScale);
               
               LargeUnrefinedAquamarine.WindOffsetter windOffsetter;
               if (largeUnrefinedAquamarine.isSuitableForWind(largeUnrefinedAquamarineConfig)
                       && largeUnrefinedAquamarine1.isSuitableForWind(largeUnrefinedAquamarineConfig))
               {
                  windOffsetter = new LargeUnrefinedAquamarine.WindOffsetter(pos.getY(), random, 
                          largeUnrefinedAquamarineConfig.windSpeed);
               } else
               {
                  windOffsetter = LargeUnrefinedAquamarine.WindOffsetter.noWind();
               }

               boolean flag = largeUnrefinedAquamarine.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(world, windOffsetter);
               boolean flag1 = largeUnrefinedAquamarine1.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(world, windOffsetter);
               if (flag)
               {
                  largeUnrefinedAquamarine.placeBlocks(world, random, windOffsetter);
               }

               if (flag1)
               {
                  largeUnrefinedAquamarine1.placeBlocks(world, random, windOffsetter);
               }

               return true;
            }
         } else
         {
            return false;
         }
      }
   }

   public static LargeUnrefinedAquamarine.LargeUnrefineAquamarine makeAquamarine(BlockPos pos, boolean b, Random random, int i, FloatProvider floatProvider, FloatProvider floatProvider1)
   {
      return new LargeUnrefinedAquamarine.LargeUnrefineAquamarine(pos, b, i, (double)floatProvider.sample(random), (double)floatProvider1.sample(random));
   }

   public void placeDebugMarkers(WorldGenLevel world, BlockPos pos, Column.Range column, LargeUnrefinedAquamarine.WindOffsetter windOffsetter)
   {
      world.setBlock(windOffsetter.offset(pos.atY(column.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
      world.setBlock(windOffsetter.offset(pos.atY(column.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

      for(BlockPos.MutableBlockPos mutablePos = pos.atY(column.floor() + 2).mutable(); mutablePos.getY() < column.ceiling() - 1;
              mutablePos.move(Direction.UP))
      {
         BlockPos pos1 = windOffsetter.offset(mutablePos);
         if (AquamarineUtils.isEmptyOrWater(world, pos1) || world.getBlockState(pos1).is(BlockInit.UNREFINED_AQUAMARINE.get()))
         {
            world.setBlock(pos1, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
         }
      }

   }

   static final class LargeUnrefineAquamarine
   {
      private BlockPos root;
      private final boolean pointingUp;
      private int radius;
      private final double bluntness;
      private final double scale;

      LargeUnrefineAquamarine(BlockPos pos, boolean b, int i, double j, double k)
      {
         this.root = pos;
         this.pointingUp = b;
         this.radius = i;
         this.bluntness = j;
         this.scale = k;
      }

      private int getHeight()
      {
         return this.getHeightAtRadius(0.0F);
      }

      private int getMinY()
      {
         return this.pointingUp ? this.root.getY() : this.root.getY() - this.getHeight();
      }

      private int getMaxY()
      {
         return !this.pointingUp ? this.root.getY() : this.root.getY() + this.getHeight();
      }

      boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel world, LargeUnrefinedAquamarine.WindOffsetter windOffsetter)
      {
         while(this.radius > 1)
         {
            BlockPos.MutableBlockPos mutablePos = this.root.mutable();
            int i = Math.min(10, this.getHeight());

            for(int j = 0; j < i; ++j) {
               if (world.getBlockState(mutablePos).is(Blocks.LAVA))
               {
                  return false;
               }

               if (AquamarineUtils.isCircleMostlyEmbeddedInStone(world, windOffsetter.offset(mutablePos), this.radius))
               {
                  this.root = mutablePos;
                  return true;
               }
               mutablePos.move(this.pointingUp ? Direction.DOWN : Direction.UP);
            }
            this.radius /= 2;
         }
         return false;
      }

      private int getHeightAtRadius(float f)
      {
         return (int)AquamarineUtils.getAquamarineHeight((double)f, (double)this.radius, this.scale, this.bluntness);
      }

      void placeBlocks(WorldGenLevel world, Random random, LargeUnrefinedAquamarine.WindOffsetter windOffsetter)
      {
         for(int i = -this.radius; i <= this.radius; ++i)
         {
            for(int j = -this.radius; j <= this.radius; ++j)
            {
               float f = Mth.sqrt((float)(i * i + j * j));
               if (!(f > (float)this.radius))
               {
                  int k = this.getHeightAtRadius(f);
                  if (k > 0)
                  {
                     if ((double)random.nextFloat() < 0.2D)
                     {
                        k = (int)((float)k * Mth.randomBetween(random, 0.8F, 1.0F));
                     }

                     BlockPos.MutableBlockPos mutablePos = this.root.offset(i, 0, j).mutable();
                     boolean flag = false;
                     int l = this.pointingUp ? 
                             world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mutablePos.getX(), mutablePos.getZ()) : Integer.MAX_VALUE;

                     for(int i1 = 0; i1 < k && mutablePos.getY() < l; ++i1)
                     {
                        BlockPos pos = windOffsetter.offset(mutablePos);
                        if (AquamarineUtils.isEmptyOrWaterOrLava(world, pos))
                        {
                           flag = true;
                           Block block = BlockInit.UNREFINED_AQUAMARINE.get();
                           world.setBlock(pos, block.defaultBlockState(), 2);
                        } else if (flag && world.getBlockState(pos).is(BASE_STONE_ULTRASPACE))
                        {
                           break;
                        }
                        mutablePos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                     }
                  }
               }
            }
         }

      }

      boolean isSuitableForWind(LargeDripstoneConfiguration config)
      {
         return this.radius >= config.minRadiusForWind && this.bluntness >= (double)config.minBluntnessForWind;
      }
   }

   static final class WindOffsetter
   {
      private final int originY;
      @Nullable
      private final Vec3 windSpeed;

      WindOffsetter(int i, Random random, FloatProvider floatProvider)
      {
         this.originY = i;
         float f = floatProvider.sample(random);
         float f1 = Mth.randomBetween(random, 0.0F, (float)Math.PI);
         this.windSpeed = new Vec3((double)(Mth.cos(f1) * f), 0.0D, (double)(Mth.sin(f1) * f));
      }

      private WindOffsetter()
      {
         this.originY = 0;
         this.windSpeed = null;
      }

      static LargeUnrefinedAquamarine.WindOffsetter noWind()
      {
         return new LargeUnrefinedAquamarine.WindOffsetter();
      }

      BlockPos offset(BlockPos pos)
      {
         if (this.windSpeed == null)
         {
            return pos;
         } else
         {
            int i = this.originY - pos.getY();
            Vec3 vec3 = this.windSpeed.scale((double)i);
            return pos.offset(vec3.x, 0.0D, vec3.z);
         }
      }
   }
}