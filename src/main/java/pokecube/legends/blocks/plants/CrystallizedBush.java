package pokecube.legends.blocks.plants;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DeadBushBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
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

	public CrystallizedBush(final Properties props)
    {
        super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(CrystallizedBush.WATERLOGGED, false));
    }

	@Override
    public VoxelShape getShape(final BlockState state, final IBlockReader block, final BlockPos pos, final ISelectionContext context) {
		final Vector3d vector = state.getOffset(block, pos);
		return CrystallizedBush.SHAPE.move(vector.x, vector.y, vector.z);
	}

	@Override
	public void entityInside(final BlockState state, final World world, final BlockPos pos, final Entity entity) {
		if (entity instanceof LivingEntity) {
			entity.makeStuckInBlock(state, new Vector3d(0.9D, 0.75D, 0.9D));
			if (!world.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
				final double d0 = Math.abs(entity.getX() - entity.xOld);
				final double d1 = Math.abs(entity.getZ() - entity.zOld);
				if (d0 >= 0.003000000026077032D || d1 >= 0.003000000026077032D) entity.hurt(DamageSource.CACTUS, 1.0F);
			}
		}
	}

	@Override
    public boolean isPathfindable(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final PathType path) {
		return false;
	}

	@Override
	protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(CrystallizedBush.WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(final BlockItemUseContext context)
	{
		final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
		return this.defaultBlockState().setValue(CrystallizedBush.WATERLOGGED, ifluidstate.is(FluidTags.WATER)
			&& ifluidstate.getAmount() == 8);
	}

	@Override
	public OffsetType getOffsetType() {
		return OffsetType.XZ;
	}

	@Override
	public boolean mayPlaceOn(final BlockState state, final IBlockReader worldIn, final BlockPos pos) {
		return state.isFaceSturdy(worldIn, pos, Direction.UP);
	}

	@Override
	public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
								  final BlockPos facingPos)
	{
		if (state.getValue(CrystallizedBush.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}

	// Adds Waterlogging
	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(final BlockState state)
	{
		return state.getValue(CrystallizedBush.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
}