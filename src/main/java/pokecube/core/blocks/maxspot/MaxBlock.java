package pokecube.core.blocks.maxspot;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.blocks.InteractableDirectionalBlock;

public class MaxBlock extends InteractableDirectionalBlock implements SimpleWaterloggedBlock
{
    private static final Map<Direction, VoxelShape> DYNAMAX  = new HashMap<>();
    private static final Map<Direction, VoxelShape> DYNAMAX_COLLISION  = new HashMap<>();
    protected static final DirectionProperty        FACING      = DirectionalBlock.FACING;
    protected static final BooleanProperty          WATERLOGGED = BlockStateProperties.WATERLOGGED;

    static
    {// @formatter:off
        DYNAMAX.put(Direction.NORTH, Shapes.or(
                Block.box(2, 2, 13, 14, 14, 16),
                Block.box(3, 3, 2, 13, 14, 13)).optimize());
        DYNAMAX.put(Direction.EAST, Shapes.or(
                Block.box(0, 2, 2, 3, 14, 14),
                Block.box(3, 3, 3, 14, 13, 13)).optimize());
        DYNAMAX.put(Direction.SOUTH, Shapes.or(
                Block.box(2, 2, 0, 14, 14, 3),
                Block.box(3, 3, 3, 13, 13, 14)).optimize());
        DYNAMAX.put(Direction.WEST, Shapes.or(
                Block.box(13, 2, 2, 16, 14, 14),
                Block.box(2, 3, 3, 13, 13, 13)).optimize());
        DYNAMAX.put(Direction.UP, Shapes.or(
                Block.box(2, 0, 2, 14, 3, 14),
                Block.box(3, 3, 3, 13, 14, 13)).optimize());
        DYNAMAX.put(Direction.DOWN, Shapes.or(
                Block.box(2, 13, 2, 14, 16, 14),
                Block.box(3, 2, 3, 13, 13, 13)).optimize());

        DYNAMAX_COLLISION.put(Direction.NORTH, Shapes.or(
                Block.box(2, 2, 13, 14, 14, 16)).optimize());
        DYNAMAX_COLLISION.put(Direction.EAST, Shapes.or(
                Block.box(0, 2, 2, 3, 14, 14)).optimize());
        DYNAMAX_COLLISION.put(Direction.SOUTH, Shapes.or(
                Block.box(2, 2, 0, 14, 14, 3)).optimize());
        DYNAMAX_COLLISION.put(Direction.WEST, Shapes.or(
                Block.box(13, 2, 2, 16, 14, 14)).optimize());
        DYNAMAX_COLLISION.put(Direction.UP, Shapes.or(
                Block.box(2, 0, 2, 14, 3, 14)).optimize());
        DYNAMAX_COLLISION.put(Direction.DOWN, Shapes.or(
                Block.box(2, 13, 2, 14, 16, 14)).optimize());
    }// @formatter:on

    public MaxBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MaxBlock.FACING, Direction.UP).setValue(
                MaxBlock.WATERLOGGED, false));
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
                               final CollisionContext context)
    {
        return DYNAMAX.get(state.getValue(MaxBlock.FACING));
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
                                        final CollisionContext context)
    {
        return DYNAMAX_COLLISION.get(state.getValue(MaxBlock.FACING));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(MaxBlock.FACING, MaxBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        final BlockState state = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        Direction direction = context.getClickedFace();
        return state.is(this) && state.getValue(MaxBlock.FACING) == direction ? (BlockState)this.defaultBlockState()
                .setValue(MaxBlock.FACING, direction.getOpposite()) : (BlockState)this.defaultBlockState()
                .setValue(MaxBlock.FACING, direction)
                .setValue(MaxBlock.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(MaxBlock.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    // Adds Waterlogging State
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(MaxBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
    {
        return new MaxTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, final boolean isMoving)
    {
        final int power = worldIn.getBestNeighborSignal(pos);
        final BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile == null || !(tile instanceof MaxTile)) return;
        final MaxTile repel = (MaxTile) tile;
        if (power != 0)
        {
            repel.enabled = false;
            repel.removeForbiddenSpawningCoord();
        }
        else
        {
            repel.enabled = true;
            repel.addForbiddenSpawningCoord();
        }
    }
}
