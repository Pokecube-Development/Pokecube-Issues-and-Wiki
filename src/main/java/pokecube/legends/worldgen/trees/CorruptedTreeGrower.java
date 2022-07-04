package pokecube.legends.worldgen.trees;

import java.util.Random;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class CorruptedTreeGrower extends AbstractTreeGrower
{
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return  Holder.direct(Trees.CORRUPTED_TREE.get());
    }
}