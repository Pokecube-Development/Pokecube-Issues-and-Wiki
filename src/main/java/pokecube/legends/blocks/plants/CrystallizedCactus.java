package pokecube.legends.blocks.plants;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.BlockInit;

public class CrystallizedCactus extends Block implements SimpleWaterloggedBlock
{
	protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
	protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public CrystallizedCactus(final Properties props)
    {
        super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(CrystallizedCactus.WATERLOGGED, false));
    }

    @Override
    public void tick(final BlockState state, final ServerLevel server, final BlockPos pos, final Random random)
    {
        if (!server.isAreaLoaded(pos, 1)) return;
        if (!state.canSurvive(server, pos)) server.destroyBlock(pos, true);
    }

	@Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
	{
	      return CrystallizedCactus.COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
	      return CrystallizedCactus.OUTLINE_SHAPE;
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader reader, final BlockPos pos)
    {
       final BlockState state1 = reader.getBlockState(pos.below());
       return (state1.isFaceSturdy(reader, pos, Direction.UP) || state1.is(BlockInit.CRYSTALLIZED_CACTUS.get())
    		   || state1.is(Blocks.CACTUS));
    }

	@Override
	public void entityInside(final BlockState state, final Level world, final BlockPos pos, final Entity entity)
	{
		entity.hurt(DamageSource.CACTUS, 1.0F);
    }

	@Override
    public boolean isPathfindable(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final PathComputationType path)
	{
		return false;
	}

	@Nullable
	@Override
	public BlockPathTypes getAiPathNodeType(final BlockState state, final BlockGetter world, final BlockPos pos, @Nullable final Mob entity)
	{
		return BlockPathTypes.DAMAGE_CACTUS;
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(CrystallizedCactus.WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(final BlockPlaceContext context)
	{
		final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
		return this.defaultBlockState().setValue(CrystallizedCactus.WATERLOGGED, ifluidstate.is(FluidTags.WATER)
			&& ifluidstate.getAmount() == 8);
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
								  final BlockPos facingPos)
	{
		if (!state.canSurvive(world, currentPos)) world.scheduleTick(currentPos, this, 1);
		if (state.getValue(CrystallizedCactus.WATERLOGGED)) world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}

	// Adds Waterlogging
	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(final BlockState state)
	{
		return state.getValue(CrystallizedCactus.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
}