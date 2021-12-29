package pokecube.legends.worldgen.features;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import pokecube.legends.blocks.normalblocks.AshLayerBlock;
import pokecube.legends.init.BlockInit;

public class AshFeature extends Feature<NoneFeatureConfiguration>
{
   public AshFeature(Codec<NoneFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
   {
       WorldGenLevel world = context.level();
       BlockPos pos = context.origin();
       BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
       BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();

       for(int i = 0; i < 16; ++i)
       {
          for(int j = 0; j < 16; ++j)
          {
             int k = pos.getX() + i;
             int l = pos.getZ() + j;
             int i1 = world.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
             mutablePos.set(k, i1, l);
             mutablePos1.set(mutablePos).move(Direction.DOWN, 1);

             world.setBlock(mutablePos, BlockInit.ASH.get().defaultBlockState().setValue(AshLayerBlock.LAYERS, 2), 2);
          }
       }
       return true;
    }
}