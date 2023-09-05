package pokecube.legends.worldgen.trees;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class InvertedTreeGrower extends AbstractTreeGrower
{
    //  TODO: Fix trees
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource randomIn, final boolean b)
    {
        if (randomIn.nextInt(10) == 0)
        {
            return null /*b ? Trees.INVERTED_TREE_FANCY.get() :  Trees.INVERTED_TREE_FANCY.get()*/;
        } else
        {
            return null /*b ? Trees.INVERTED_TREE.get() :  Trees.INVERTED_TREE.get()*/;
        }
    }
}
