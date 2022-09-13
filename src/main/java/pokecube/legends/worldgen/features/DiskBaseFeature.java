package pokecube.legends.worldgen.features;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskBaseFeature extends Feature<DiskConfiguration>
{
    public DiskBaseFeature(Codec<DiskConfiguration> config)
    {
        super(config);
    }

    public boolean place(FeaturePlaceContext<DiskConfiguration> context)
    {
        DiskConfiguration diskconfiguration = context.config();
        BlockPos blockpos = context.origin();
        WorldGenLevel worldgenlevel = context.level();
        RandomSource randomsource = context.random();
        boolean flag = false;
        int i = blockpos.getY();
        int j = i + diskconfiguration.halfHeight();
        int k = i - diskconfiguration.halfHeight() - 1;
        int l = diskconfiguration.radius().sample(randomsource);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-l, 0, -l), blockpos.offset(l, 0, l)))
        {
            int i1 = blockpos1.getX() - blockpos.getX();
            int j1 = blockpos1.getZ() - blockpos.getZ();
            if (i1 * i1 + j1 * j1 <= l * l)
            {
                flag |= this.placeColumn(diskconfiguration, worldgenlevel, randomsource, j, k,
                        blockpos$mutableblockpos.set(blockpos1));
            }
        }

        return flag;
    }

    protected boolean placeColumn(DiskConfiguration config, WorldGenLevel level, RandomSource random, int top,
            int bottom, BlockPos.MutableBlockPos pos)
    {
        boolean flag = false;
        BlockState blockstate = null;
        for (int i = top; i > bottom; --i)
        {
            pos.setY(i);
            if (config.target().test(level, pos))
            {
                // FIXME this used to check particular ultraspace stuff!
                blockstate = config.stateProvider().getState(level, random, pos);
                level.setBlock(pos, blockstate, 2);
                this.markAboveForPostProcessing(level, pos);
                flag = true;
            }
        }
        return flag;
    }

//   public boolean place(FeaturePlaceContext<DiskConfiguration> context)
//   {
//      DiskConfiguration diskConfig = context.config();
//      BlockPos pos = context.origin();
//      WorldGenLevel world = context.level();
//      boolean flag = false;
//      int i = pos.getY();
//      int j = i + diskConfig.halfHeight();
//      int k = i - diskConfig.halfHeight() - 1;
//      int l = diskConfig.radius().sample(context.random());
//
//      for(int i1 = pos.getX() - l; i1 <= pos.getX() + l; ++i1)
//      {
//         for(int j1 = pos.getZ() - l; j1 <= pos.getZ() + l; ++j1)
//         {
//            int k1 = i1 - pos.getX();
//            int l1 = j1 - pos.getZ();
//            if (k1 * k1 + l1 * l1 <= l * l)
//            {
//               boolean flag2 = false;
//
//               for(int i2 = j; i2 >= k; --i2)
//               {
//                  BlockPos pos1 = new BlockPos(i1, i2, j1);
//                  BlockState state = world.getBlockState(pos1);
//                  Block block = state.getBlock();
//                  boolean flag3 = false;
//                  if (i2 > k) {
//                     for(BlockState stateTarget : diskConfig.targets())
//                     {
//                        if (stateTarget.is(block)) {
//                           world.setBlock(pos1, diskConfig.state(), 2);
//                           this.markAboveForPostProcessing(world, pos1);
//                           flag = true;
//                           flag3 = true;
//                           break;
//                        }
//                     }
//                  }
//
//                  if (flag1 && flag2 && state.isAir())
//                  {
//                     BlockState stateSand = diskConfig.state().is(BlockInit.ASH_BLOCK.get()) ? BlockInit.ASH_BLOCK.get().defaultBlockState()
//                             : diskConfig.state().is(BlockInit.AZURE_SAND.get()) ? BlockInit.AZURE_SANDSTONE.get().defaultBlockState() 
//                             : diskConfig.state().is(BlockInit.BLACKENED_SAND.get()) ? BlockInit.BLACKENED_SANDSTONE.get().defaultBlockState()
//                                     : diskConfig.state().is(BlockInit.CRYSTALLIZED_SAND.get()) ? BlockInit.CRYSTALLIZED_SANDSTONE.get().defaultBlockState()
//                                     : BlockInit.ULTRA_STONE.get().defaultBlockState();
//                     world.setBlock(new BlockPos(i1, i2 + 1, j1), stateSand, 2);
//                  }
//                  flag2 = flag3;
//               }
//            }
//         }
//      }
//      return flag;
//   }
}