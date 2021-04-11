package pokecube.core.blocks.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
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
import pokecube.core.blocks.InteractableHorizontalBlock;

public class TraderBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> TRADER = new HashMap<>();
    private static final DirectionProperty          FACING       = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED  = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
        TRADER.put(Direction.NORTH, VoxelShapes.or(
            Block.box(0, 0, 3, 16, 1, 16),
            Block.box(1, 1, 4, 15, 10, 15),
            Block.box(0, 10, 3, 16, 11, 16),
            Block.box(10, 11, 7, 15, 12, 12),
            Block.box(6, 11, 8, 10, 12, 11),
            Block.box(1, 11, 7, 6, 12, 12),
            Block.box(4, 11, 14, 12, 16, 15)).optimize());
        TRADER.put(Direction.EAST, VoxelShapes.or(
            Block.box(0, 0, 0, 13, 1, 16),
            Block.box(1, 1, 1, 12, 10, 15),
            Block.box(0, 10, 0, 13, 11, 16),
            Block.box(4, 11, 10, 9, 12, 15),
            Block.box(5, 11, 6, 8, 12, 10),
            Block.box(4, 11, 1, 9, 12, 6),
            Block.box(1, 11, 4, 2, 16, 12)).optimize());
        TRADER.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(0, 0, 0, 16, 1, 13),
            Block.box(1, 1, 1, 15, 10, 12),
            Block.box(0, 10, 0, 16, 11, 13),
            Block.box(1, 11, 4, 6, 12, 9),
            Block.box(6, 11, 5, 10, 12, 8),
            Block.box(10, 11, 4, 15, 12, 9),
            Block.box(4, 11, 1, 12, 16, 2)).optimize());
        TRADER.put(Direction.WEST, VoxelShapes.or(
            Block.box(3, 0, 0, 16, 1, 16),
            Block.box(4, 1, 1, 15, 10, 15),
            Block.box(3, 10, 0, 16, 11, 16),
            Block.box(7, 11, 1, 12, 12, 6),
            Block.box(8, 11, 6, 11, 12, 10),
            Block.box(7, 11, 10, 12, 12, 15),
            Block.box(14, 11, 4, 15, 16, 12)).optimize());
    }

    public TraderBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TraderBlock.FACING, Direction.NORTH).setValue(
            TraderBlock.WATERLOGGED, false));
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
                               final ISelectionContext context)
    {
        return TraderBlock.TRADER.get(state.getValue(TraderBlock.FACING));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(TraderBlock.FACING, TraderBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(TraderBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(TraderBlock.WATERLOGGED, ifluidstate.is(
                        FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(TraderBlock.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    // Adds Waterlogging State
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(TraderBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TraderTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

}
