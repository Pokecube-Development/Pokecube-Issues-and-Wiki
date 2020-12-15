package pokecube.legends.blocks.customblocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

public class SpectrumGlass extends GlassBlock
{
    public SpectrumGlass(final String name, final Properties props)
    {
        super(props);
    }

	@Override
	public boolean shouldDisplayFluidOverlay(final BlockState state, final IBlockDisplayReader world, final BlockPos pos, final FluidState fluidstate) {
		return true;
	}
}