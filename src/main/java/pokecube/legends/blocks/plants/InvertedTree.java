package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import pokecube.legends.worldgen.trees.Trees;

public class InvertedTree extends Tree
{
    @Override
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        if (randomIn.nextInt(10) == 0)
        {
            return b ? Trees.INVERTED_TREE_FANCY : Trees.INVERTED_TREE_FANCY;
        } else
        {
            return b ? Trees.INVERTED_TREE : Trees.INVERTED_TREE;
        }
    }

}
