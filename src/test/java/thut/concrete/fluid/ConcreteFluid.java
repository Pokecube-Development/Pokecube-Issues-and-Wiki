package thut.concrete.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class ConcreteFluid extends ForgeFlowingFluid
{
    public ConcreteFluid(Properties properties)
    {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(LEVEL, 8));
    }

    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
    {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }

    public int getAmount(FluidState state)
    {
        return state.getValue(LEVEL);
    }

    public boolean isSource(FluidState state)
    {
        return getAmount(state) == 8;
    }
    
    @Override
    public void tick(Level level, BlockPos pos, FluidState state)
    {
        super.tick(level, pos, state);
        System.out.println(level.getBlockState(pos)+" "+level.getFluidState(pos));
    }

    @Override
    public float getOwnHeight(FluidState p_76048_)
    {
        return (float) p_76048_.getAmount() / 8.0F;
    }
}
