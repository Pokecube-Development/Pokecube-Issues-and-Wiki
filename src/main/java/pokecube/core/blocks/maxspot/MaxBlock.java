package pokecube.core.blocks.maxspot;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableDirectionalBlock;

import java.util.HashMap;
import java.util.Map;

public class MaxBlock extends InteractableDirectionalBlock implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> DYNAMAX  = new HashMap<>();
    private static final Map<Direction, VoxelShape> DYNAMAX_COLLISION  = new HashMap<>();
    protected static final DirectionProperty        FACING      = DirectionalBlock.FACING;
    protected static final BooleanProperty          WATERLOGGED = BlockStateProperties.WATERLOGGED;

    static
    {// @formatter:off
        DYNAMAX.put(Direction.NORTH, VoxelShapes.or(
                Block.box(2, 2, 13, 14, 14, 16),
                Block.box(3, 3, 2, 13, 14, 13)).optimize());
        DYNAMAX.put(Direction.EAST, VoxelShapes.or(
                Block.box(0, 2, 2, 3, 14, 14),
                Block.box(3, 3, 3, 14, 13, 13)).optimize());
        DYNAMAX.put(Direction.SOUTH, VoxelShapes.or(
                Block.box(2, 2, 0, 14, 14, 3),
                Block.box(3, 3, 3, 13, 13, 14)).optimize());
        DYNAMAX.put(Direction.WEST, VoxelShapes.or(
                Block.box(13, 2, 2, 16, 14, 14),
                Block.box(2, 3, 3, 13, 13, 13)).optimize());
        DYNAMAX.put(Direction.UP, VoxelShapes.or(
                Block.box(2, 0, 2, 14, 3, 14),
                Block.box(3, 3, 3, 13, 14, 13)).optimize());
        DYNAMAX.put(Direction.DOWN, VoxelShapes.or(
                Block.box(2, 13, 2, 14, 16, 14),
                Block.box(3, 2, 3, 13, 13, 13)).optimize());

        DYNAMAX_COLLISION.put(Direction.NORTH, VoxelShapes.or(
                Block.box(2, 2, 13, 14, 14, 16)).optimize());
        DYNAMAX_COLLISION.put(Direction.EAST, VoxelShapes.or(
                Block.box(0, 2, 2, 3, 14, 14)).optimize());
        DYNAMAX_COLLISION.put(Direction.SOUTH, VoxelShapes.or(
                Block.box(2, 2, 0, 14, 14, 3)).optimize());
        DYNAMAX_COLLISION.put(Direction.WEST, VoxelShapes.or(
                Block.box(13, 2, 2, 16, 14, 14)).optimize());
        DYNAMAX_COLLISION.put(Direction.UP, VoxelShapes.or(
                Block.box(2, 0, 2, 14, 3, 14)).optimize());
        DYNAMAX_COLLISION.put(Direction.DOWN, VoxelShapes.or(
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
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
                               final ISelectionContext context)
    {
        return DYNAMAX.get(state.getValue(MaxBlock.FACING));
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
                                        final ISelectionContext context)
    {
        return DYNAMAX_COLLISION.get(state.getValue(MaxBlock.FACING));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(MaxBlock.FACING, MaxBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
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
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
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
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new MaxTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public void neighborChanged(final BlockState state, final World worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, final boolean isMoving)
    {
        final int power = worldIn.getBestNeighborSignal(pos);
        final TileEntity tile = worldIn.getBlockEntity(pos);
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
