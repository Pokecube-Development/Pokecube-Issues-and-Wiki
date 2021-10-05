package pokecube.core.blocks.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
import pokecube.core.blocks.InteractableHorizontalBlock;

public class TraderBlock extends InteractableHorizontalBlock implements SimpleWaterloggedBlock, EntityBlock
{
    private static final Map<Direction, VoxelShape> TRADER = new HashMap<>();
    private static final DirectionProperty          FACING       = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED  = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
        TraderBlock.TRADER.put(Direction.NORTH, Shapes.or(
            Block.box(0, 0, 3, 16, 1, 16),
            Block.box(1, 1, 4, 15, 10, 15),
            Block.box(0, 10, 3, 16, 11, 16),
            Block.box(10, 11, 7, 15, 12, 12),
            Block.box(6, 11, 8, 10, 12, 11),
            Block.box(1, 11, 7, 6, 12, 12),
            Block.box(4, 11, 14, 12, 16, 15)).optimize());
        TraderBlock.TRADER.put(Direction.EAST, Shapes.or(
            Block.box(0, 0, 0, 13, 1, 16),
            Block.box(1, 1, 1, 12, 10, 15),
            Block.box(0, 10, 0, 13, 11, 16),
            Block.box(4, 11, 10, 9, 12, 15),
            Block.box(5, 11, 6, 8, 12, 10),
            Block.box(4, 11, 1, 9, 12, 6),
            Block.box(1, 11, 4, 2, 16, 12)).optimize());
        TraderBlock.TRADER.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 0, 0, 16, 1, 13),
            Block.box(1, 1, 1, 15, 10, 12),
            Block.box(0, 10, 0, 16, 11, 13),
            Block.box(1, 11, 4, 6, 12, 9),
            Block.box(6, 11, 5, 10, 12, 8),
            Block.box(10, 11, 4, 15, 12, 9),
            Block.box(4, 11, 1, 12, 16, 2)).optimize());
        TraderBlock.TRADER.put(Direction.WEST, Shapes.or(
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
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
                               final CollisionContext context)
    {
        return TraderBlock.TRADER.get(state.getValue(TraderBlock.FACING));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TraderBlock.FACING, TraderBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(TraderBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(TraderBlock.WATERLOGGED, ifluidstate.is(
                        FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
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
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new TraderTile(pos, state);
    }

}
