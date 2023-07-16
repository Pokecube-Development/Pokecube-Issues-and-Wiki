package pokecube.legends.worldgen.trees;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class TemporalTreeGrower extends AbstractMegaTreeGrower
{
	  @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return Holder.direct(Trees.TEMPORAL_TREE.get());
    }

    @Nullable
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(Random randomIn)
    {
        return Holder.direct(Trees.MEGA_TEMPORAL_TREE.get());
    }
}
