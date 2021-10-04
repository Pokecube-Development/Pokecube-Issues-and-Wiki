package pokecube.legends.blocks.plants;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class TallCrystallizedBush extends DoublePlantBlock implements SimpleWaterloggedBlock
{
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	public TallCrystallizedBush(final Properties props)
    {
        super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(TallCrystallizedBush.WATERLOGGED, false)
			.setValue(TallCrystallizedBush.HALF, DoubleBlockHalf.LOWER));
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

	@Nullable
	@Override
	public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, @Nullable Mob entity)
	{
		return BlockPathTypes.DAMAGE_OTHER;
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(TallCrystallizedBush.HALF, TallCrystallizedBush.WATERLOGGED);
	}

	@Override
	public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
							final LivingEntity placer, final ItemStack stack)
	{
		if (placer != null)
		{
			final FluidState fluidState = world.getFluidState(pos.above());
			world.setBlock(pos.above(), state.setValue(TallCrystallizedBush.HALF, DoubleBlockHalf.UPPER)
				.setValue(TallCrystallizedBush.WATERLOGGED, fluidState.getType() == Fluids.WATER), 1);
		}
	}

	@Override
	public BlockState getStateForPlacement(final BlockPlaceContext context)
	{
		final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
		final BlockPos pos = context.getClickedPos();

		final BlockPos tallBushPos = this.getTallBushTopPos(pos);
		if (pos.getY() < 255 && tallBushPos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(context))
			return this.defaultBlockState().setValue(TallCrystallizedBush.HALF, DoubleBlockHalf.LOWER)
			.setValue(TallCrystallizedBush.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate.getAmount() == 8);

		return null;
	}

	// Breaking leaves water if underwater
	private void removeHalf(final Level world, final BlockPos pos, final BlockState state, final Player player)
	{
		final BlockState blockstate = world.getBlockState(pos);
		final FluidState fluidState = world.getFluidState(pos);
		if (fluidState.getType() == Fluids.WATER) world.setBlock(pos, fluidState.createLegacyBlock(), 35);
		else
		{
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			world.levelEvent(player, 2001, pos, Block.getId(blockstate));
		}
	}
	@Override
	public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state,
								  final Player player)
	{
		final BlockPos tallBushPos = this.getTallBushPos(pos, state.getValue(TallCrystallizedBush.HALF));
		BlockState tallBushBlockState = world.getBlockState(tallBushPos);
		if (tallBushBlockState.getBlock() == this && !pos.equals(tallBushPos)) this.removeHalf(world, tallBushPos,
			tallBushBlockState, player);
		final BlockPos tallBushPartPos = this.getTallBushTopPos(tallBushPos);
		tallBushBlockState = world.getBlockState(tallBushPartPos);
		if (tallBushBlockState.getBlock() == this && !pos.equals(tallBushPartPos)) this.removeHalf(world, tallBushPartPos,
			tallBushBlockState, player);
		super.playerWillDestroy(world, pos, state, player);
	}

	private BlockPos getTallBushTopPos(final BlockPos pos)
	{
		return pos.above();
	}

	private BlockPos getTallBushPos(final BlockPos pos, final DoubleBlockHalf part)
	{
		if (part == DoubleBlockHalf.LOWER) return pos;
		else return pos.below();
	}

	@Override
	public boolean mayPlaceOn(final BlockState state, final BlockGetter worldIn, final BlockPos pos) {
		return state.isFaceSturdy(worldIn, pos, Direction.UP);
	}

	@Override
	public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
								  final BlockPos facingPos)
	{
		if (state.getValue(TallCrystallizedBush.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}

	// Adds Waterlogging
	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(final BlockState state)
	{
		return state.getValue(TallCrystallizedBush.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
}