package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;
import pokecube.legends.init.BlockInit;

import java.util.Random;

public class OreBlock extends Block
{
    public OreBlock(final Properties properties)
    {
        super(properties);
    }

    public int xpOnDrop(Random random) {
        if (this == BlockInit.ULTRA_COAL_ORE.get()) {
            return MathHelper.nextInt(random, 0, 2);
        } else if (this == BlockInit.ULTRA_DIAMOND_ORE.get()) {
            return MathHelper.nextInt(random, 3, 7);
        } else if (this == BlockInit.ULTRA_EMERALD_ORE.get()) {
            return MathHelper.nextInt(random, 3, 7);
        } else if (this == BlockInit.ULTRA_LAPIS_ORE.get()) {
            return MathHelper.nextInt(random, 2, 5);
        } else if (this == BlockInit.ULTRA_COSMIC_DUST_ORE.get()) {
            return MathHelper.nextInt(random, 1, 3);
        } else if (this == BlockInit.FRACTAL_ORE.get()) {
            return MathHelper.nextInt(random, 3, 7);
        } else if (this == BlockInit.RUBY_ORE.get()) {
            return MathHelper.nextInt(random, 3, 7);
        } else {
            return this == BlockInit.SAPPHIRE_ORE.get() ? MathHelper.nextInt(random, 3, 7) : 0;
        }
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack) {
        super.spawnAfterBreak(state, world, pos, stack);
    }

    @Override
    public int getExpDrop(BlockState p_getExpDrop_1_, IWorldReader p_getExpDrop_2_, BlockPos p_getExpDrop_3_, int p_getExpDrop_4_, int p_getExpDrop_5_) {
        return p_getExpDrop_5_ == 0 ? this.xpOnDrop(this.RANDOM) : 0;
    }
}
