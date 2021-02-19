package pokecube.adventures.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IWorldReader;

public class LaboratoryGlass extends GlassBlock
{
    public LaboratoryGlass(final Properties properties, final MaterialColor color)
    {
        super(properties);
    }

	@Override
	public boolean shouldDisplayFluidOverlay(final BlockState state, final IBlockDisplayReader world, final BlockPos pos, final FluidState fluidstate) {
		return true;
	}

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.62f, 0.85f, 1.00f};
    }
}