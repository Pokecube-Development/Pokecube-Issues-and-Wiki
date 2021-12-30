package pokecube.legends.worldgen.features;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import pokecube.legends.blocks.normalblocks.AshLayerBlock;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.WorldgenFeatures;

public class AshLayer1Feature extends Feature<NoneFeatureConfiguration>
{
   public AshLayer1Feature(Codec<NoneFeatureConfiguration> config)
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
             
             world.setBlock(mutablePos, BlockInit.ASH.get().defaultBlockState().setValue(AshLayerBlock.LAYERS, 1), 1);
             BlockState state = world.getBlockState(mutablePos1);
             if (state.hasProperty(SnowyDirtBlock.SNOWY))
             {
                 world.setBlock(mutablePos1, state.setValue(SnowyDirtBlock.SNOWY, Boolean.valueOf(false)), 2);
             }
          }
       }
       return true;
    }
}