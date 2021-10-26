package pokecube.legends.blocks.plants;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.PlantType;

public class CrystallizedBush extends DeadBushBlock implements SimpleWaterloggedBlock
{
	private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public CrystallizedBush(final Properties props)
    {
        super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(CrystallizedBush.WATERLOGGED, false));
    }

	@Override
    public VoxelShape getShape(final BlockState state, final BlockGetter block, final BlockPos pos, final CollisionContext context) {
		final Vec3 vector = state.getOffset(block, pos);
		return CrystallizedBush.SHAPE.move(vector.x, vector.y, vector.z);
	}

	@Override
	public void entityInside(final BlockState state, final Level world, final BlockPos pos, final Entity entity) {
		if (entity instanceof LivingEntity) {
			entity.makeStuckInBlock(state, new Vec3(0.9D, 0.75D, 0.9D));
			if (!world.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
				final double d0 = Math.abs(entity.getX() - entity.xOld);
				final double d1 = Math.abs(entity.getZ() - entity.zOld);
				if (d0 >= 0.003000000026077032D || d1 >= 0.003000000026077032D) entity.hurt(DamageSource.CACTUS, 1.0F);
			}
		}
	}

	@Override
	public boolean isPathfindable(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final PathComputationType path)
	{
		return false;
	}

	@Nullable
	@Override
	public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, @Nullable Mob entity)
	{
		return BlockPathTypes.DAMAGE_OTHER;
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(CrystallizedBush.WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(final BlockPlaceContext context)
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
	public boolean mayPlaceOn(final BlockState state, final BlockGetter worldIn, final BlockPos pos) {
		return state.isFaceSturdy(worldIn, pos, Direction.UP);
	}

	@Override
	public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
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

	@Override
	public PlantType getPlantType(BlockGetter world, BlockPos pos) {
	    return PlantType.DESERT;
	}
}