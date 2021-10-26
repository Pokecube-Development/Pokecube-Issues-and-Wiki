package pokecube.adventures.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LaboratoryGlassBlock extends StainedGlassBlock
{
    public LaboratoryGlassBlock(DyeColor color, final Properties properties)
    {
        super(color, properties);
    }

	@Override
	public boolean shouldDisplayFluidOverlay(final BlockState state, final BlockAndTintGetter world, final BlockPos pos, final FluidState fluidstate) {
		return true;
	}

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.62f, 0.85f, 1.00f};
    }
}