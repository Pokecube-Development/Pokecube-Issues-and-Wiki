package pokecube.core.blocks.berries;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.items.berries.BerryManager;

public class BerryLeaf extends LeavesBlock
{
    final int index;

    public BerryLeaf(final Properties properties, final int index)
    {
        super(properties);
        this.index = index;
        this.setDefaultState(this.stateContainer.getBaseState().with(LeavesBlock.DISTANCE, Integer.valueOf(1)).with(
                LeavesBlock.PERSISTENT, Boolean.valueOf(false)));
    }

    @Override
    public boolean isFoliage(final BlockState state, final IWorldReader world, final BlockPos pos)
    {
        return true;
    }

    @Override
    public void randomTick(final BlockState state, final World worldIn, final BlockPos pos, final Random random)
    {
        super.randomTick(state, worldIn, pos, random);
        final Block fruit = BerryManager.berryFruits.get(this.index);
        if (fruit != null && worldIn.isAirBlock(pos.down()) && random.nextInt(PokecubeCore
                .getConfig().leafBerryTicks) == 0) worldIn.setBlockState(pos.down(), fruit.getDefaultState());
    }

    @Override
    public boolean ticksRandomly(final BlockState state)
    {
        return true;
    }
}
