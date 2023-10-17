package pokecube.adventures.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LaboratoryGlassBlock extends StainedGlassBlock implements BeaconBeamBlock
{
    public LaboratoryGlassBlock(DyeColor color, final Properties properties)
    {
        super(color, properties);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.62f, 0.85f, 1.00f};
    }
}