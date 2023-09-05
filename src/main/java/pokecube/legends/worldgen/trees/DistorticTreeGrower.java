package pokecube.legends.worldgen.trees;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DistorticTreeGrower extends AbstractTreeGrower
{
//  TODO: Fix trees
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(final RandomSource randomIn, final boolean b)
    {
        return null /*Trees.DISTORTIC_TREE.get()*/;
    }
}