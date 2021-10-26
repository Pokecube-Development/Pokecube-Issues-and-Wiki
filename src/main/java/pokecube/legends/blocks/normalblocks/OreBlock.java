package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import pokecube.legends.init.BlockInit;

public class OreBlock extends Block
{
    public OreBlock(final Properties properties)
    {
        super(properties);
    }

    public int xpOnDrop(final Random random)
    {
        if (this == BlockInit.ULTRA_COAL_ORE.get()) return MathHelper.nextInt(random, 0, 2);
        else if (this == BlockInit.ULTRA_DIAMOND_ORE.get()) return MathHelper.nextInt(random, 3, 7);
        else if (this == BlockInit.ULTRA_EMERALD_ORE.get()) return MathHelper.nextInt(random, 3, 7);
        else if (this == BlockInit.ULTRA_LAPIS_ORE.get()) return MathHelper.nextInt(random, 2, 5);
        else if (this == BlockInit.ULTRA_COSMIC_DUST_ORE.get()) return MathHelper.nextInt(random, 1, 3);
        else if (this == BlockInit.FRACTAL_ORE.get()) return MathHelper.nextInt(random, 3, 7);
        else if (this == BlockInit.RUBY_ORE.get()) return MathHelper.nextInt(random, 3, 7);
        else return this == BlockInit.SAPPHIRE_ORE.get() ? MathHelper.nextInt(random, 3, 7) : 0;
    }

    @Override
    public int getExpDrop(final BlockState p_getExpDrop_1_, final IWorldReader p_getExpDrop_2_, final BlockPos p_getExpDrop_3_,
            final int p_getExpDrop_4_, final int p_getExpDrop_5_)
    {
        return p_getExpDrop_5_ == 0 ? this.xpOnDrop(this.RANDOM) : 0;
    }
}
