package pokecube.legends.worldgen.trees;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class AgedTreeGrower extends AbstractMegaTreeGrower 
{
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource random, final boolean b)
    {
        return random.nextBoolean() ? Trees.AGED_SPRUCE_TREE : Trees.AGED_PINE_TREE;
    }

    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource random)
    {
        return random.nextBoolean() ? Trees.MEGA_AGED_SPRUCE_TREE : Trees.MEGA_AGED_PINE_TREE;
    }
}