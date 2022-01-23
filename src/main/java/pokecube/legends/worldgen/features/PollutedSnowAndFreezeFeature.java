package pokecube.legends.worldgen.features;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import pokecube.legends.blocks.normalblocks.AshLayerBlock;
import pokecube.legends.init.BlockInit;

public class PollutedSnowAndFreezeFeature extends Feature<NoneFeatureConfiguration>
{
   public PollutedSnowAndFreezeFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
   {
      WorldGenLevel world = context.level();
      BlockPos pos = context.origin();
      BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
      BlockPos.MutableBlockPos mutablePosDown = new BlockPos.MutableBlockPos();

      for(int i = 0; i < 16; ++i)
      {
         for(int j = 0; j < 16; ++j)
         {
            int k = pos.getX() + i;
            int l = pos.getZ() + j;
            int i1 = world.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
            mutablePos.set(k, i1, l);
            mutablePosDown.set(mutablePos).move(Direction.DOWN, 1);
            Biome biome = world.getBiome(mutablePos);
            BlockState state = world.getBlockState(mutablePosDown);
            if (biome.coldEnoughToSnow(mutablePosDown) && state.getBlock() == Blocks.ICE)
            {
               world.setBlock(mutablePosDown, BlockInit.CORRUPTED_DIRT.get().defaultBlockState(), 2);
            }

            if (biome.shouldSnow(world, mutablePos))
            {
               world.setBlock(mutablePos, BlockInit.ASH.get().defaultBlockState().setValue(AshLayerBlock.LAYERS, 2), 2);
               if (state.hasProperty(SnowyDirtBlock.SNOWY))
               {
                  world.setBlock(mutablePosDown, state.setValue(SnowyDirtBlock.SNOWY, Boolean.valueOf(true)), 2);
               }
            }
         }
      }
      return true;
   }
}