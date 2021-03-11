package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import pokecube.legends.worldgen.trees.Trees;

public class Ultra_Tree02 extends Tree {

	@Override
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return Trees.ULTRA_TREE02;
    }
}
