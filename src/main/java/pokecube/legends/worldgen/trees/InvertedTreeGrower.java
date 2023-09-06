package pokecube.legends.worldgen.trees;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class InvertedTreeGrower extends AbstractTreeGrower
{
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource randomIn, final boolean b)
    {
        if (randomIn.nextInt(10) == 0)
        {
            return b ? Trees.FANCY_INVERTED_TREE_BEES_005 :  Trees.FANCY_INVERTED_TREE;
        } else
        {
            return b ? Trees.INVERTED_TREE_BEES_005 :  Trees.INVERTED_TREE;
        }
    }
}
