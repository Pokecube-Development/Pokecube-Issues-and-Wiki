package pokecube.legends.worldgen.trees;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class AgedTreeGrower extends AbstractMegaTreeGrower 
{
	  @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(final Random randomIn, final boolean b)
    {
        return randomIn.nextBoolean() ? Holder.direct(Trees.AGED_SPRUCE_TREE.get()) : Holder.direct(Trees.AGED_PINE_TREE.get());
    }

    @Nullable
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(Random randomIn)
    {
        return randomIn.nextBoolean() ? Holder.direct(Trees.MEGA_AGED_SPRUCE_TREE.get()) : Holder.direct(Trees.MEGA_AGED_PINE_TREE.get());
    }
}