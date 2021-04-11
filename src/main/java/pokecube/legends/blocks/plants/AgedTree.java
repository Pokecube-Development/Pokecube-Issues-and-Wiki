package pokecube.legends.blocks.plants;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.trees.BigTree;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import pokecube.legends.worldgen.trees.Trees;

public class AgedTree extends BigTree {

	@Override
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return Trees.AGED_TREE;
    }

    @Nullable
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredMegaFeature(Random randomIn)
    {
        return randomIn.nextBoolean() ? Trees.MEGA_AGED_SPRUCE_TREE : Trees.MEGA_AGED_PINE_TREE;
    }
}