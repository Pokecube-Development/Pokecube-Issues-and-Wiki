package thut.concrete.block;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class LiquidBase extends LiquidBlock implements BucketPickup, IFluidBlock
{
    private boolean fluidStateCacheInitialized = false;
    private final List<FluidState> stateCache;

    public LiquidBase(Properties p_54695_, Supplier<? extends FlowingFluid> p_54694_)
    {
        super(p_54694_, p_54695_);
        this.stateCache = Lists.newArrayList();
    }

    protected synchronized void initFluidStateCache()
    {
        if (fluidStateCacheInitialized == false)
        {
            this.stateCache.add(getFluid().getSource(false));

            for (int i = 1; i < 15; ++i) this.stateCache.add(getFluid().getFlowing(15 - i, false));

            this.stateCache.add(getFluid().getFlowing(15, true));
            fluidStateCacheInitialized = true;
        }
    }

    @Override
    public FluidState getFluidState(BlockState p_54765_)
    {
        int i = p_54765_.getValue(LEVEL);
        if (!fluidStateCacheInitialized) initFluidStateCache();
        return this.stateCache.get(Math.min(i, 15));
    }

    @Override
    public int place(Level world, BlockPos pos, FluidStack fluidStack, FluidAction action)
    {
        // TODO Auto-generated method stub
        return 15;
    }

    @Override
    public FluidStack drain(Level world, BlockPos pos, FluidAction action)
    {
        // TODO Auto-generated method stub
        return FluidStack.EMPTY;
    }

    @Override
    public boolean canDrain(Level world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public float getFilledPercentage(Level world, BlockPos pos)
    {
        BlockState block = world.getBlockState(pos);
        Optional<Integer> opt = block.getOptionalValue(LEVEL);
        return opt.isEmpty() ? 0 : opt.get() / 15f;
    }
    
    
}
