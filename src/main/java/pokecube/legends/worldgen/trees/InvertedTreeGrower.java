package pokecube.legends.worldgen.trees;

import java.util.Random;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class InvertedTreeGrower extends AbstractTreeGrower
{
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        if (randomIn.nextInt(10) == 0)
        {
            return b ?  Holder.direct(Trees.INVERTED_TREE_FANCY.get()) :  Holder.direct(Trees.INVERTED_TREE_FANCY.get());
        } else
        {
            return b ?  Holder.direct(Trees.INVERTED_TREE.get()) :  Holder.direct(Trees.INVERTED_TREE.get());
        }
    }
}
