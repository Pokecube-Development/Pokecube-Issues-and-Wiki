package pokecube.core.blocks.berries;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.blocks.berries.BerryGenManager.TreeGrower;
import pokecube.core.items.berries.BerryManager;

public class BerryCrop extends CropsBlock
{
    final int index;

    public BerryCrop(final Properties builder, final int index)
    {
        super(builder);
        this.index = index;
    }

    @Override
    protected IItemProvider getSeedsItem()
    {
        return BerryManager.berryItems.get(this.index);
    }

    @Override
    public boolean ticksRandomly(final BlockState state)
    {
        // Unlike vanilla crops, we still do something when max age
        return true;
    }

    @Override
    public void randomTick(final BlockState state, final ServerWorld worldIn, final BlockPos pos, final Random random)
    {
        super.randomTick(state, worldIn, pos, random);
        if (!worldIn.isAreaLoaded(pos, 1)) return;
        final int age = this.getAge(worldIn.getBlockState(pos));
        if (age == this.getMaxAge())
        {
            final TreeGrower grower = BerryGenManager.trees.get(this.index);
            final BlockPos up = pos.up();
            if (grower != null) grower.growTree(worldIn, pos, this.index);
            else if (worldIn.isAirBlock(up)) worldIn.setBlockState(up, BerryManager.berryFruits.get(this.index)
                    .getDefaultState());
        }
    }

    @Override
    public void grow(final ServerWorld worldIn, final Random rand, final BlockPos pos, final BlockState state)
    {
        super.grow(worldIn, rand, pos, state);
        final int age = this.getAge(worldIn.getBlockState(pos));
        if (age == this.getMaxAge())
        {
            final TreeGrower grower = BerryGenManager.trees.get(this.index);
            final BlockPos up = pos.up();
            if (grower != null) grower.growTree(worldIn, pos, this.index);
            else if (worldIn.isAirBlock(up)) worldIn.setBlockState(up, BerryManager.berryFruits.get(this.index)
                    .getDefaultState());
        }
    }
}
