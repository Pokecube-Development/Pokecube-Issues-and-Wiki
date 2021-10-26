package thut.api.block;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IViscousFluid
{
    /**
     * How much difference is needed for this fluid to flow.
     * 
     * @return
     */
    public int getFlowDifferential(World world, BlockPos pos, BlockState state, Random rand);
}
