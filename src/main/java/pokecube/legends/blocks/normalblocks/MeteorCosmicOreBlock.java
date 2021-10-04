package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.init.BlockInit;

public class MeteorCosmicOreBlock extends MeteorBlock
{
    public MeteorCosmicOreBlock(final int num, final Properties properties)
    {
        super(num, properties);
    }

    public int xpOnDrop(final Random random)
    {
        return this == BlockInit.METEOR_COSMIC_DUST_ORE.get() ? Mth.nextInt(random, 1, 3) : 0;
    }

    @Override
    public int getExpDrop(final BlockState p_getExpDrop_1_, final LevelReader p_getExpDrop_2_,
            final BlockPos p_getExpDrop_3_, final int p_getExpDrop_4_, final int p_getExpDrop_5_)
    {
        return p_getExpDrop_5_ == 0 ? this.xpOnDrop(this.RANDOM) : 0;
    }
}
