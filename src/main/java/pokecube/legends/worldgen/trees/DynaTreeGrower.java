package pokecube.legends.worldgen.trees;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DynaTreeGrower extends AbstractTreeGrower
{
    //  TODO: Fix trees
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource random, final boolean b)
    {
        return TreeFeatures.OAK /*Trees.DYNA_TREE.get()*/;
    }
}