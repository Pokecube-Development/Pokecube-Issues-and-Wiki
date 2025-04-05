package pokecube.adventures.blocks;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.StainedGlassBlock;

public class LaboratoryGlassBlock extends StainedGlassBlock implements BeaconBeamBlock
{
    public LaboratoryGlassBlock(DyeColor color, final Properties properties)
    {
        super(color, properties);
    }
// TODO see if this was needed?
//    @Override
//    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
//        return new float[]{0.62f, 0.85f, 1.00f};
//    }
}