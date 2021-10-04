package pokecube.legends.blocks.plants;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import pokecube.legends.worldgen.trees.Trees;

public class TemporalTree extends AbstractMegaTreeGrower {

	@Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return Trees.TEMPORAL_TREE;
    }

    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random randomIn)
    {
        return Trees.MEGA_TEMPORAL_TREE;
    }
}
