package pokecube.legends.worldgen.features;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import pokecube.legends.init.BlockInit;

public class DiskBaseFeature extends Feature<DiskConfiguration>
{
   public DiskBaseFeature(Codec<DiskConfiguration> config)
   {
      super(config);
   }

   public boolean place(FeaturePlaceContext<DiskConfiguration> context)
   {
      DiskConfiguration diskConfig = context.config();
      BlockPos pos = context.origin();
      WorldGenLevel world = context.level();
      boolean flag = false;
      int i = pos.getY();
      int j = i + diskConfig.halfHeight();
      int k = i - diskConfig.halfHeight() - 1;
      boolean flag1 = diskConfig.state().getBlock() instanceof FallingBlock;
      int l = diskConfig.radius().sample(context.random());

      for(int i1 = pos.getX() - l; i1 <= pos.getX() + l; ++i1)
      {
         for(int j1 = pos.getZ() - l; j1 <= pos.getZ() + l; ++j1)
         {
            int k1 = i1 - pos.getX();
            int l1 = j1 - pos.getZ();
            if (k1 * k1 + l1 * l1 <= l * l)
            {
               boolean flag2 = false;

               for(int i2 = j; i2 >= k; --i2)
               {
                  BlockPos pos1 = new BlockPos(i1, i2, j1);
                  BlockState state = world.getBlockState(pos1);
                  Block block = state.getBlock();
                  boolean flag3 = false;
                  if (i2 > k) {
                     for(BlockState stateTarget : diskConfig.targets())
                     {
                        if (stateTarget.is(block)) {
                           world.setBlock(pos1, diskConfig.state(), 2);
                           this.markAboveForPostProcessing(world, pos1);
                           flag = true;
                           flag3 = true;
                           break;
                        }
                     }
                  }

                  if (flag1 && flag2 && state.isAir())
                  {
                     BlockState stateSand = diskConfig.state().is(BlockInit.ASH_BLOCK.get()) ? BlockInit.ASH_BLOCK.get().defaultBlockState()
                             : diskConfig.state().is(BlockInit.AZURE_SAND.get()) ? BlockInit.AZURE_SANDSTONE.get().defaultBlockState() 
                             : diskConfig.state().is(BlockInit.BLACKENED_SAND.get()) ? BlockInit.BLACKENED_SANDSTONE.get().defaultBlockState()
                                     : BlockInit.CRYSTALLIZED_SANDSTONE.get().defaultBlockState();
                     world.setBlock(new BlockPos(i1, i2 + 1, j1), stateSand, 2);
                  }
                  flag2 = flag3;
               }
            }
         }
      }
      return flag;
   }
}