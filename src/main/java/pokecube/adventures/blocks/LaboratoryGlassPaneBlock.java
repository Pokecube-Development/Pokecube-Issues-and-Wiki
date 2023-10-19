package pokecube.adventures.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LaboratoryGlassPaneBlock extends StainedGlassPaneBlock implements BeaconBeamBlock
{
    public LaboratoryGlassPaneBlock(DyeColor color, final Properties properties)
    {
        super(color, properties);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.62f, 0.85f, 1.00f};
    }
}