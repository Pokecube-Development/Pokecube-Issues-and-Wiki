package pokecube.legends.worldgen.trees;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DynaTreeGrower extends AbstractTreeGrower
{
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource random, final boolean b)
    {
        return Trees.DYNA_TREE;
    }
}