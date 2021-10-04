package thut.api.block;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IViscousFluid
{
    /**
     * How much difference is needed for this fluid to flow.
     * 
     * @return
     */
    public int getFlowDifferential(Level world, BlockPos pos, BlockState state, Random rand);
}
