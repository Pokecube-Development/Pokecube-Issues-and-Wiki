package pokecube.core.blocks.tms;

import java.util.HashMap;
import java.util.Map;

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

public class TMBlock extends InteractableHorizontalBlock implements SimpleWaterloggedBlock, EntityBlock
{
    private static final Map<Direction, VoxelShape> TM_MACHINE  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box @formatter:off
    static
    {
        TMBlock.TM_MACHINE.put(Direction.NORTH, Shapes.or(
                Block.box(0, 0, 3, 16, 1, 16),
                Block.box(1, 1, 4, 15, 10, 15),
                Block.box(0, 10, 3, 16, 11, 16),
                Block.box(2, 7, 1, 6, 8, 4),
                Block.box(2, 11, 7, 14, 12, 12),
                Block.box(4, 11, 14, 12, 16, 15)).optimize());
        TMBlock.TM_MACHINE.put(Direction.EAST, Shapes.or(
                Block.box(0, 0, 0, 13, 1, 16),
                Block.box(1, 1, 1, 12, 10, 15),
                Block.box(0, 10, 0, 13, 11, 16),
                Block.box(12, 7, 2, 15, 8, 6),
                Block.box(4, 11, 2, 9, 12, 14),
                Block.box(1, 11, 4, 2, 16, 12)).optimize());
        TMBlock.TM_MACHINE.put(Direction.SOUTH, Shapes.or(
                Block.box(0, 0, 0, 16, 1, 13),
                Block.box(1, 1, 1, 15, 10, 12),
                Block.box(0, 10, 0, 16, 11, 13),
                Block.box(10, 7, 12, 14, 8, 15),
                Block.box(2, 11, 4, 14, 12, 9),
                Block.box(4, 11, 1, 12, 16, 2)).optimize());
        TMBlock.TM_MACHINE.put(Direction.WEST, Shapes.or(
                Block.box(3, 0, 0, 16, 1, 16),
                Block.box(4, 1, 1, 15, 10, 15),
                Block.box(3, 10, 0, 16, 11, 16),
                Block.box(1, 7, 10, 4, 8, 14),
                Block.box(7, 11, 2, 12, 12, 14),
                Block.box(14, 11, 4, 15, 16, 12)).optimize());
    }

    // Precise selection box @formatter:on
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return TMBlock.TM_MACHINE.get(state.getValue(TMBlock.FACING));
    }

    public TMBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TMBlock.FACING, Direction.NORTH).setValue(
                TMBlock.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TMBlock.FACING, TMBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return (super.getStateForPlacement(context)).setValue(TMBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(TMBlock.WATERLOGGED, ifluidstate.is(
                        FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    // Adds Waterlogging State
    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState,
            final LevelAccessor world, final BlockPos currentPos, final BlockPos facingPos)
    {
        if (state.getValue(TMBlock.WATERLOGGED)) world.scheduleTick(currentPos, Fluids.WATER,
                Fluids.WATER.getTickDelay(world));

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(TMBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new TMTile(pos, state);
    }
}
