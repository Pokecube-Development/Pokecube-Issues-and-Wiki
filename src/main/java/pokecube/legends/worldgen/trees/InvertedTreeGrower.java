package pokecube.legends.worldgen.trees;

import java.util.Random;

import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class InvertedTreeGrower extends AbstractTreeGrower
{
    @Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(final Random randomIn, final boolean b)
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
