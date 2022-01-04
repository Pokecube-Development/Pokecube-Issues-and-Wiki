package pokecube.legends.worldgen.trees;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class AgedTreeGrower extends AbstractMegaTreeGrower 
{
	  @Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return randomIn.nextBoolean() ? Trees.AGED_SPRUCE_TREE : Trees.AGED_PINE_TREE;
    }

    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random randomIn)
    {
        return randomIn.nextBoolean() ? Trees.MEGA_AGED_SPRUCE_TREE : Trees.MEGA_AGED_PINE_TREE;
    }
}