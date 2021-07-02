package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.BlockInit;

import java.util.Random;

public class MeteorCosmicOreBlock extends MeteorBlock
{
    public MeteorCosmicOreBlock(int num, final Properties properties)
    {
        super(num, properties);
    }

    public int xpOnDrop(Random random) {
        return this == BlockInit.METEOR_COSMIC_DUST_ORE.get() ? MathHelper.nextInt(random, 1, 3) : 0;
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
