package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class GlassBlockBase extends TransparentBlock
{
    public GlassBlockBase(DyeColor color, final Properties props)
    {
        super(props);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(final BlockState state, final BlockAndTintGetter block, final BlockPos pos,
            final FluidState fluidState)
    {
        return true;
    }

    @Override
    public @Nullable Integer getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos,
            BlockPos beaconPos)
    {
        return super.getBeaconColorMultiplier(state, level, pos, beaconPos);
    }
}