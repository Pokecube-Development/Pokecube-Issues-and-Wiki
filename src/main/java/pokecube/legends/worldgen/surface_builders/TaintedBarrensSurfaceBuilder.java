package pokecube.legends.worldgen.surface_builders;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import pokecube.legends.init.BlockInit;

public class TaintedBarrensSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration>
{
    public TaintedBarrensSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> config)
    {
        super(config);
    }

    @Override
    public void apply(Random random, ChunkAccess chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState block,
            BlockState fluid, int seaLevel, int num, long seed, SurfaceBuilderBaseConfiguration config)
    {
        double d0 = Biome.BIOME_INFO_NOISE.getValue((double)x * 0.25D, (double)z * 0.25D, false);
        if (d0 > 0.0D) {
           int i = x & 15;
           int j = z & 15;
           BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();

           for(int k = startHeight; k >= num; --k) {
              mutablePos2.set(i, k, j);
              if (!chunk.getBlockState(mutablePos2).isAir()) {
                 if (k == 62 && !chunk.getBlockState(mutablePos2).is(fluid.getBlock())) {
                    chunk.setBlockState(mutablePos2, fluid, false);
                 }
                 break;
              }
           }
        }

        SurfaceBuilder.DEFAULT.apply(random, chunk, biome, x, z, startHeight, noise, block, fluid, seaLevel, num, seed, config);

        this.apply(random, chunk, biome, x, z, startHeight, noise, block, fluid, config.getTopMaterial(), config.getUnderMaterial(), config.getUnderwaterMaterial(), seaLevel, num);
    }

    public void apply(Random random, ChunkAccess chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState block,
            BlockState fluid, BlockState top, BlockState middle, BlockState bottom, int seaLevel, int num)
    {

          BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
          int i = (int)(noise / 3.0D + 3.0D + random.nextDouble() * 0.25D);
          if (i == 0)
          {
              boolean flag = false;
              for(int j = startHeight; j >= num; --j)
              {
                  pos.set(x, j, z);
                  BlockState blockstate = chunk.getBlockState(pos);
                  if (blockstate.isAir())
                  {
                      flag = false;
                  } else if (blockstate.is(block.getBlock()))
                  {
                      if (!flag) {
                          BlockState blockstate1;
                          if (j >= seaLevel)
                          {
                              blockstate1 = Blocks.AIR.defaultBlockState();
                          } else if (j == seaLevel - 1)
                          {
                              blockstate1 = biome.getTemperature(pos) < 0.15F ? Blocks.ICE.defaultBlockState() : fluid;
                          } else if (j >= seaLevel - (7 + i))
                          {
                              blockstate1 = block;
                          } else
                          {
                              blockstate1 = bottom;
                          }
                          chunk.setBlockState(pos, blockstate1, false);
                      }
                      flag = true;
                  }
              }
          } else
          {
        	  BlockState blockstate3 = middle;
              int k = -1;

              for(int l = startHeight; l >= num; --l)
              {
            	  pos.set(x, l, z);
            	  BlockState blockstate4 = chunk.getBlockState(pos);
            	  if (blockstate4.isAir())
            	  {
            		  k = -1;
            	  } else if (blockstate4.is(block.getBlock()))
            	  {
            		  if (k == -1)
            		  {
            			  k = i;
            			  BlockState blockstate2;
            			  if (l >= seaLevel + 2)
            			  {
            				  blockstate2 = top;
            			  } else if (l >= seaLevel - 1)
            			  {
            				  blockstate3 = middle;
            				  blockstate2 = top;
            			  } else if (l >= seaLevel - 4)
            			  {
            				  blockstate3 = middle;
            				  blockstate2 = middle;
            			  } else if (l >= seaLevel - (7 + i))
            			  {
            				  blockstate2 = blockstate3;
            			  } else
            			  {
            				  blockstate3 = block;
            				  blockstate2 = bottom;
            			  }
            			  chunk.setBlockState(pos, blockstate2, false);
            		  } else if (k > 0)
            		  {
            			  --k;
            			  chunk.setBlockState(pos, blockstate3, false);
            			  if (k == 0 && blockstate3.is(BlockInit.CORRUPTED_DIRT.get()) && i > 1)
            			  {
            				  k = random.nextInt(4) + Math.max(0, l - seaLevel);
            				  blockstate3 = BlockInit.CORRUPTED_DIRT.get().defaultBlockState();
            			  }
            		  }
            	  }
              }
          }
     }
}
