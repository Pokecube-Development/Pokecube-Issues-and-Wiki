package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class DistorticOneWaySpectrumGlass extends DistorticOneWayStainedGlass
{
    protected static final DirectionProperty FACING = DirectionalBlock.FACING;

    public DistorticOneWaySpectrumGlass(final String name, DyeColor color, final Properties properties)
    {
        super(name, color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.97f, 0.45f, 0.24f};
    }
}