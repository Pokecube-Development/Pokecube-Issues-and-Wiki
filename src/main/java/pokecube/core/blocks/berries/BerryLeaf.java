package pokecube.core.blocks.berries;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.items.berries.BerryManager;

public class BerryLeaf extends LeavesBlock
{
    final int index;

    public BerryLeaf(final Properties properties, final int index)
    {
        super(properties);
        this.index = index;
    }

    @Override
    public void randomTick(final BlockState state, final ServerWorld worldIn, final BlockPos pos, final Random random)
    {
        super.randomTick(state, worldIn, pos, random);
        if (random.nextInt(PokecubeCore.getConfig().leafBerryTicks) != 0) return;
        if (state.getValue(LeavesBlock.PERSISTENT)) return;
        final Block fruit = BerryManager.berryFruits.get(this.index);
        if (fruit != null && worldIn.isEmptyBlock(pos.below())) worldIn.setBlockAndUpdate(pos.below(), fruit.defaultBlockState());
    }

    @Override
    public boolean isRandomlyTicking(final BlockState state)
    {
        return true;
    }
}
