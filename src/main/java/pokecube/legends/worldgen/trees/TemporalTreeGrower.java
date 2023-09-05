package pokecube.legends.worldgen.trees;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class TemporalTreeGrower extends AbstractMegaTreeGrower
{
    //  TODO: Fix trees
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource randomIn, final boolean b)
    {
        return null /*Trees.TEMPORAL_TREE.get()*/;
    }

    @Nullable
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource randomIn)
    {
        return null /*Trees.MEGA_TEMPORAL_TREE.get()*/;
    }
}
