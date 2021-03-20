package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CrystallizedBush extends DeadBushBlock implements IWaterLoggable
{
	private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public CrystallizedBush(final String name, final Properties props)
    {
        super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

	public VoxelShape getShape(BlockState state, IBlockReader block, BlockPos pos, ISelectionContext context) {
		Vector3d vector = state.getOffset(block, pos);
		return SHAPE.move(vector.x, vector.y, vector.z);
	}
	
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if ((entityIn instanceof PlayerEntity)) {
			entityIn.hurt(DamageSource.CACTUS, 1.0F);
		}
    }

	@Override
	protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(final BlockItemUseContext context)
	{
		final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
		return this.defaultBlockState().setValue(WATERLOGGED, ifluidstate.is(FluidTags.WATER)
			&& ifluidstate.getAmount() == 8);
	}

	@Override
	public OffsetType getOffsetType() {
		return OffsetType.XZ;
	}

	@Override
	public boolean mayPlaceOn(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return state.isFaceSturdy(worldIn, pos, Direction.UP);
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
								  final BlockPos facingPos)
	{
		if (state.getValue(WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}

	// Adds Waterlogging
	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(final BlockState state)
	{
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
}