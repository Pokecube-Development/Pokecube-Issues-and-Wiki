package pokecube.legends.worldgen.trees;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class AgedTreeGrower extends AbstractMegaTreeGrower 
{
//    TODO: Fix trees
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource randomIn, final boolean b)
    {
        return null /*randomIn.nextBoolean() ?  Trees.AGED_SPRUCE_TREE.get() : Trees.AGED_PINE_TREE.get()*/;
    }

    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource randomIn)
    {
        return null /*randomIn.nextBoolean() ? Trees.MEGA_AGED_SPRUCE_TREE.get() : Trees.MEGA_AGED_PINE_TREE.get()*/;
    }
}