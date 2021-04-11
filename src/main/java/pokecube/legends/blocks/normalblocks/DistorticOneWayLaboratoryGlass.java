package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class DistorticOneWayLaboratoryGlass extends DistorticOneWayStainedGlass
{
    protected static final DirectionProperty FACING = DirectionalBlock.FACING;

    public DistorticOneWayLaboratoryGlass(DyeColor color, final Properties properties)
    {
        super(color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.62f, 0.85f, 1.00f};
    }
}