package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.block.trees.BigTree;
import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import pokecube.legends.worldgen.trees.Trees;

import javax.annotation.Nullable;

public class TemporalTree extends BigTree {

	@Override
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return Trees.TEMPORAL_TREE;
    }

    @Nullable
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredMegaFeature(Random randomIn)
    {
        return Trees.MEGA_TEMPORAL_TREE;
    }
}
