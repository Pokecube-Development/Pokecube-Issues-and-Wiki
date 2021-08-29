package pokecube.legends.blocks.plants;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathNodeType;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CrystallizedCactus extends Block implements IWaterLoggable
{  
	protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
	protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public CrystallizedCactus(final Properties props)
    {
        super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }
	
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
	      return COLLISION_SHAPE;
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
	      return OUTLINE_SHAPE;
    }

	@Override
	public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
		entity.hurt(DamageSource.CACTUS, 1.0F);
    }

	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType path) {
		return false;
	}

	@Nullable
	@Override
	public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, @Nullable MobEntity entity)
	{
		return PathNodeType.DAMAGE_CACTUS;
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