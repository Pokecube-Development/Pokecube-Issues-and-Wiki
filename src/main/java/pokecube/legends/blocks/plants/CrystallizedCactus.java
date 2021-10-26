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
import net.minecraft.world.level.material.Material;
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
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    public void tick(BlockState state, ServerLevel server, BlockPos pos, Random random) 
    {
        if (!server.isAreaLoaded(pos, 1)) return;
        if (!state.canSurvive(server, pos)) {
        	server.destroyBlock(pos, true);
        }
    }
	
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) 
	{
	      return COLLISION_SHAPE;
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) 
    {
	      return OUTLINE_SHAPE;
    }

    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) 
    {
       BlockState blockstate1 = reader.getBlockState(pos.below());
       return (blockstate1.isFaceSturdy(reader, pos, Direction.UP) || blockstate1.is(BlockInit.CRYSTALLIZED_CACTUS.get()) 
    		   || blockstate1.is(Blocks.CACTUS));
    }

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) 
	{
		entity.hurt(DamageSource.CACTUS, 1.0F);
    }

	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType path) 
	{
		return false;
	}

	@Nullable
	@Override
	public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, @Nullable Mob entity)
	{
		return BlockPathTypes.DAMAGE_CACTUS;
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(final BlockPlaceContext context)
	{
		final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
		return this.defaultBlockState().setValue(WATERLOGGED, ifluidstate.is(FluidTags.WATER)
			&& ifluidstate.getAmount() == 8);
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
								  final BlockPos facingPos)
	{   
		if (!state.canSurvive(world, currentPos)) 
	    {
			world.getBlockTicks().scheduleTick(currentPos, this, 1);
        }
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