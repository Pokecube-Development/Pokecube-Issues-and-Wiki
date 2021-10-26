package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import pokecube.legends.worldgen.trees.Trees;

public class MirageTree extends AbstractTreeGrower
{	
	@Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return Trees.MIRAGE_TREE;
    }
}