package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;

public class TallTaintedSeagrassBlock extends TallSeagrassBlock implements LiquidBlockContainer, BonemealableBlock
{
    public TallTaintedSeagrassBlock(final BlockBehaviour.Properties config)
    {
        super(config);
    }

    @Override
    public ItemStack getCloneItemStack(final BlockGetter block, final BlockPos pos, final BlockState state)
    {
        return new ItemStack(PlantsInit.TALL_TAINTED_SEAGRASS.get());
    }

    @Override
    public boolean mayPlaceOn(final BlockState state, final BlockGetter block, final BlockPos pos)
    {
        return (state.isFaceSturdy(block, pos, Direction.UP) || state.is(BlockInit.CRYSTALLIZED_SAND.get())) && !state
                .is(Blocks.MAGMA_BLOCK);
    }

    @Override
    public boolean isValidBonemealTarget(final BlockGetter block, final BlockPos pos, final BlockState state, final boolean b)
    {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(final Level world, final Random random, final BlockPos pos, final BlockState state)
    {
        return true;
    }

    @Override
    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos, final BlockState state)
    {
        Block.popResource(world, pos, new ItemStack(this));
    }
}