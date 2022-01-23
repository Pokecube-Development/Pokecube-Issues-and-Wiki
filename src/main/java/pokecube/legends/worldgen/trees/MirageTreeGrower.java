package pokecube.legends.worldgen.trees;

import java.util.Random;

import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MirageTreeGrower extends AbstractTreeGrower
{	
	  @Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return Trees.MIRAGE_TREE;
    }
}