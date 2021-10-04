package pokecube.legends.blocks.plants;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import pokecube.legends.worldgen.trees.Trees;

public class AgedTree extends AbstractMegaTreeGrower {

	@Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return Trees.AGED_TREE;
    }

    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random randomIn)
    {
        return randomIn.nextBoolean() ? Trees.MEGA_AGED_SPRUCE_TREE : Trees.MEGA_AGED_PINE_TREE;
    }
}