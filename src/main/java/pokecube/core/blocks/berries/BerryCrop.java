package pokecube.core.blocks.berries;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.berries.BerryGenManager.TreeGrower;
import pokecube.core.items.berries.BerryManager;

public class BerryCrop extends CropBlock
{
    final int index;

    public BerryCrop(final Properties builder, final int index)
    {
        super(builder);
        this.index = index;
    }

    @Override
    protected ItemLike getBaseSeedId()
    {
        return BerryManager.berryItems.get(this.index);
    }

    @Override
    public boolean isRandomlyTicking(final BlockState state)
    {
        // Unlike vanilla crops, we still do something when max age
        return true;
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final Random random)
    {
        super.randomTick(state, worldIn, pos, random);
        if (!worldIn.isPositionEntityTicking(pos)) return;
        final int age = this.getAge(worldIn.getBlockState(pos));
        if (age == this.getMaxAge())
        {
            final TreeGrower grower = BerryGenManager.trees.get(this.index);
            final BlockPos up = pos.above();
            if (grower != null) grower.growTree(worldIn, pos, this.index);
            else if (worldIn.isEmptyBlock(up))
                worldIn.setBlockAndUpdate(up, BerryManager.berryFruits.get(this.index).defaultBlockState());
        }
    }

    @Override
    public void performBonemeal(final ServerLevel worldIn, final Random rand, final BlockPos pos,
            final BlockState state)
    {
        super.performBonemeal(worldIn, rand, pos, state);
        final int age = this.getAge(worldIn.getBlockState(pos));
        if (age == this.getMaxAge())
        {
            final TreeGrower grower = BerryGenManager.trees.get(this.index);
            final BlockPos up = pos.above();
            if (grower != null) grower.growTree(worldIn, pos, this.index);
            else if (worldIn.isEmptyBlock(up))
                worldIn.setBlockAndUpdate(up, BerryManager.berryFruits.get(this.index).defaultBlockState());
        }
    }
}
