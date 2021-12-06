package pokecube.legends.worldgen.features;

import com.mojang.serialization.Codec;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskFeature extends DiskBaseFeature
{
   public DiskFeature(Codec<DiskConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<DiskConfiguration> context)
   {
      return !context.level().getFluidState(context.origin()).is(FluidTags.WATER) ? false : super.place(context);
   }
}