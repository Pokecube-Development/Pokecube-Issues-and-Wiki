package pokecube.legends.worldgen.features;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class PatchFeature extends DiskBaseFeature
{
   public PatchFeature(Codec<DiskConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<DiskConfiguration> context)
   {
      WorldGenLevel world = context.level();
      ChunkGenerator chunk = context.chunkGenerator();
      Random random = context.random();
      DiskConfiguration diskConfig = context.config();

      BlockPos pos;
      for(pos = context.origin(); world.isEmptyBlock(pos) && pos.getY() > world.getMinBuildHeight() + 2; pos = pos.below())
      {}

      return world.getBlockState(pos).is(Blocks.AIR) ? false
              : super.place(new FeaturePlaceContext<>(context.topFeature(), world, context.chunkGenerator(), context.random(), pos, context.config()));
   }
}