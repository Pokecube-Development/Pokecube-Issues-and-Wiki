package thut.concrete.fluid;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import thut.api.block.flowing.IFlowingBlock;

public class DummyLiquidBlock extends LiquidBlock
{
    final Supplier<? extends Block> realBlock;

    public DummyLiquidBlock(Supplier<? extends FlowingFluid> supplier, Supplier<? extends Block> realBlock,
            Properties properties)
    {
        super(supplier, properties);
        this.realBlock = realBlock;
    }

    @Override
    public void tick(BlockState p_60462_, ServerLevel p_60463_, BlockPos p_60464_, Random p_60465_)
    {

    }

    @Override
    public void onPlace(BlockState us, Level level, BlockPos pos, BlockState other, boolean bool)
    {
        if (!(other.getBlock() instanceof IFlowingBlock b))
        {
            level.setBlock(pos, realBlock.get().defaultBlockState(), 3);
            return;
        }
        else
        {
            level.setBlock(pos, b.empty(other), 3);
        }
    }
}
