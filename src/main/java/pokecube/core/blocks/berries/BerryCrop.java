package pokecube.core.blocks.berries;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public void grow(final World worldIn, final Random rand, BlockPos pos, final BlockState state)
    {
        super.grow(worldIn, rand, pos, state);
        final int age = worldIn.getBlockState(pos).get(CropsBlock.AGE);
        if (age > 6)
        {
            final TreeGrower grower = BerryGenManager.trees.get(this.index);
            if (grower != null) grower.growTree(worldIn, pos, this.index);
            else if (worldIn.isAirBlock(pos = pos.up())) worldIn.setBlockState(pos, BerryManager.berryFruits.get(
                    this.index).getDefaultState());
        }
    }
}
