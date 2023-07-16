package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.IcebergFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

public class PollutedIcebergFeature extends IcebergFeature
{
   public PollutedIcebergFeature(Codec<BlockStateConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<BlockStateConfiguration> context)
   {
       BlockPos pos = context.origin();
       WorldGenLevel world = context.level();
       pos = new BlockPos(pos.getX(), context.chunkGenerator().getSeaLevel(), pos.getZ());
       Random random = context.random();
       boolean flag = random.nextDouble() > 0.7D;
       BlockState state = (context.config()).state;
       double d0 = random.nextDouble() * 2.0D * Math.PI;
       int i = 11 - random.nextInt(5);
       int j = 3 + random.nextInt(3);
       boolean flag1 = random.nextDouble() > 0.7D;
       int k = 11;
       int l = flag1 ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
       if (!flag1 && random.nextDouble() > 0.9D)
       {
          l += random.nextInt(19) + 7;
       }

       int i1 = Math.min(l + random.nextInt(k), 18);
       int j1 = Math.min(l + random.nextInt(7) - random.nextInt(5), k);
       int k1 = flag1 ? i : 11;

       for(int l1 = -k1; l1 < k1; ++l1)
       {
          for(int i2 = -k1; i2 < k1; ++i2)
          {
             for(int j2 = 0; j2 < l; ++j2)
             {
                int k2 = flag1 ? this.heightDependentRadiusEllipse(j2, l, j1) : this.heightDependentRadiusRound(random, j2, l, j1);
                if (flag1 || l1 < k2)
                {
                   this.generateIcebergBlock(world, random, pos, l, l1, j2, i2, k2, k1, flag1, j, d0, flag, state);
                }
             }
          }
       }

       this.smooth(world, pos, j1, l, flag1, i);

       for(int i3 = -k1; i3 < k1; ++i3)
       {
          for(int j3 = -k1; j3 < k1; ++j3)
          {
             for(int k3 = -1; k3 > -i1; --k3)
             {
                int l3 = flag1 ? Mth.ceil((float)k1 * (1.0F - (float)Math.pow((double)k3, 2.0D) / ((float)i1 * 8.0F))) : k1;
                int l2 = this.heightDependentRadiusSteep(random, -k3, i1, j1);
                if (i3 < l2) {
                   this.generateIcebergBlock(world, random, pos, i1, i3, k3, j3, l2, l3, flag1, j, d0, flag, state);
                }
             }
          }
       }

       boolean flag2 = flag1 ? random.nextDouble() > 0.1D : random.nextDouble() > 0.7D;
       if (flag2)
       {
          this.generateCutOut(random, world, j1, l, pos, flag1, i, d0, j);
       }
       return true;
   }

    public void carve(int w, int x, BlockPos pos, LevelAccessor world, boolean b, double d, BlockPos pos1, int y, int z)
    {
       int i = w + 1 + y / 3;
       int j = Math.min(w - 3, 3) + z / 2 - 1;

       for(int k = -i; k < i; ++k)
       {
          for(int l = -i; l < i; ++l)
          {
             double d0 = this.signedDistanceEllipse(k, l, pos1, i, j, d);
             if (d0 < 0.0D)
             {
                BlockPos posOffset = pos.offset(k, x, l);
                BlockState stateOffset = world.getBlockState(posOffset);
                if (isIcebergState(stateOffset) || stateOffset.is(Blocks.SNOW_BLOCK))
                {
                   if (b)
                   {
                      this.setBlock(world, posOffset, Blocks.WATER.defaultBlockState());
                   } else
                   {
                      this.setBlock(world, posOffset, Blocks.AIR.defaultBlockState());
                      this.removeFloatingSnowLayer(world, posOffset);
                   }
                }
             }
          }
       }
    }

    public void removeFloatingSnowLayer(LevelAccessor world, BlockPos pos)
    {
       if (world.getBlockState(pos.above()).is(Blocks.SNOW))
       {
          this.setBlock(world, pos.above(), Blocks.AIR.defaultBlockState());
       }
    }

    public void setIcebergBlock(BlockPos pos, LevelAccessor world, Random random, int x, int y, boolean b, boolean b1, BlockState state)
    {
       BlockState stateWorld = world.getBlockState(pos);
       if (stateWorld.getMaterial() == Material.AIR || stateWorld.is(Blocks.SNOW_BLOCK) || stateWorld.is(Blocks.ICE) || stateWorld.is(Blocks.WATER))
       {
          boolean flag = !b || random.nextDouble() > 0.05D;
          int i = b ? 3 : 2;
          if (b1 && !stateWorld.is(Blocks.WATER) && (double)x <= (double)random.nextInt(Math.max(1, y / i)) + (double)y * 0.6D && flag)
          {
             this.setBlock(world, pos, Blocks.SNOW_BLOCK.defaultBlockState());
          } else
          {
             this.setBlock(world, pos, state);
          }
       }

    }

    public static boolean isIcebergState(BlockState state)
    {
       return state.is(Blocks.PACKED_ICE) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.BLUE_ICE);
    }

    public void smooth(LevelAccessor world, BlockPos pos, int x, int y, boolean b, int z)
    {
       int i = b ? z : x / 2;

       for(int j = -i; j <= i; ++j)
       {
          for(int k = -i; k <= i; ++k)
          {
             for(int l = 0; l <= y; ++l)
             {
                BlockPos posOffset = pos.offset(j, l, k);
                BlockState stateOffset = world.getBlockState(posOffset);
                if (isIcebergState(stateOffset) || stateOffset.is(Blocks.SNOW))
                {
                   if (this.belowIsAir(world, posOffset))
                   {
                      this.setBlock(world, posOffset, Blocks.AIR.defaultBlockState());
                      this.setBlock(world, posOffset.above(), Blocks.AIR.defaultBlockState());
                   } else if (isIcebergState(stateOffset))
                   {
                      BlockState[] stateDirectional = new BlockState[]{world.getBlockState(posOffset.west()), 
                              world.getBlockState(posOffset.east()), world.getBlockState(posOffset.north()), world.getBlockState(posOffset.south())};
                      int i1 = 0;

                      for(BlockState state : stateDirectional)
                      {
                         if (!isIcebergState(state))
                         {
                            ++i1;
                         }
                      }

                      if (i1 >= 3)
                      {
                         this.setBlock(world, posOffset, Blocks.AIR.defaultBlockState());
                      }
                   }
                }
             }
          }
       }
    }
}