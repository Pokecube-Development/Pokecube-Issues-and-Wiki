package pokecube.legends.worldgen.trees;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class TemporalTreeGrower extends AbstractMegaTreeGrower
{
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource randomIn, final boolean b)
    {
        return Trees.TEMPORAL_TREE;
    }

    @Nullable
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource randomIn)
    {
        return Trees.MEGA_TEMPORAL_TREE;
    }
}
