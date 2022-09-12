package pokecube.legends.worldgen.trees;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class TemporalTreeGrower extends AbstractMegaTreeGrower
{
	  @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource randomIn, final boolean b)
    {
        return Holder.direct(Trees.TEMPORAL_TREE.get());
    }

    @Nullable
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource randomIn)
    {
        return Holder.direct(Trees.MEGA_TEMPORAL_TREE.get());
    }
}
