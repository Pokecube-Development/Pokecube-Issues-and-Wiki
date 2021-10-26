package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;

public class OneWayStainedGlass extends StainedGlassBlock
{
    protected static final DirectionProperty FACING = DirectionalBlock.FACING;

    public OneWayStainedGlass(DyeColor color, final Properties properties)
    {
        super(color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        Direction direction = context.getNearestLookingDirection().getOpposite();
        return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction));
    }

	@Override
	public boolean shouldDisplayFluidOverlay(final BlockState state, final BlockAndTintGetter world, final BlockPos pos, final FluidState fluidstate) {
		return true;
	}
}