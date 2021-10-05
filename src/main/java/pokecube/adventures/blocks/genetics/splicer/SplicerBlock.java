package pokecube.adventures.blocks.genetics.splicer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.blocks.InteractableHorizontalBlock;
import thut.api.block.ITickTile;

public class SplicerBlock extends InteractableHorizontalBlock implements SimpleWaterloggedBlock, EntityBlock
{
    private static final Map<Direction, VoxelShape> SPLICER = new HashMap<>();
    public static final DirectionProperty           FACING  = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty             FIXED   = BooleanProperty.create("fixed");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	SplicerBlock.SPLICER.put(Direction.NORTH,
		Stream.of(
				Block.box(0, 0, 3, 16, 10, 16),
				Block.box(0, 10, 11, 8, 16, 16),
				Block.box(10, 10, 11, 14, 12, 15),
				Block.box(10, 10, 4, 14, 11, 8),
				Block.box(11, 12, 12, 13, 16, 14),
				Block.box(10, 13.5, 6, 14, 15.5, 13),
				Block.box(11, 12, 5, 13, 16, 7),
				Block.box(1, 10, 3, 7, 15, 4),
				Block.box(0, 10, 4, 8, 11, 11),
				Block.box(5, 11, 9, 6, 15, 10),
				Block.box(6, 11, 7, 7, 15, 8),
				Block.box(5, 11, 5, 6, 15, 6),
				Block.box(1, 11, 7, 2, 15, 8)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.EAST,
    	Stream.of(
    	        Block.box(0, 0, 0, 13, 10, 16),
    	        Block.box(0, 10, 0, 5, 16, 8),
    	        Block.box(1, 10, 10, 5, 12, 14),
    	        Block.box(8, 10, 10, 12, 11, 14),
    	        Block.box(2, 12, 11, 4, 16, 13),
    	        Block.box(3, 13.5, 10, 10, 15.5, 14),
    	        Block.box(9, 12, 11, 11, 16, 13),
    	        Block.box(12, 10, 1, 13, 15, 7),
    	        Block.box(5, 10, 0, 12, 11, 8),
    	        Block.box(6, 11, 5, 7, 15, 6),
    	        Block.box(8, 11, 6, 9, 15, 7),
    	        Block.box(10, 11, 5, 11, 15, 6),
    	        Block.box(8, 11, 1, 9, 15, 2)
    	        ).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.SOUTH,
		Stream.of(
				Block.box(0, 0, 0, 16, 10, 13),
				Block.box(8, 10, 0, 16, 16, 5),
				Block.box(2, 10, 1, 6, 12, 5),
				Block.box(2, 10, 8, 6, 11, 12),
				Block.box(3, 12, 2, 5, 16, 4),
				Block.box(2, 13.5, 3, 6, 15.5, 10),
				Block.box(3, 12, 9, 5, 16, 11),
				Block.box(9, 10, 12, 15, 15, 13),
				Block.box(8, 10, 5, 16, 11, 12),
				Block.box(10, 11, 6, 11, 15, 7),
				Block.box(9, 11, 8, 10, 15, 9),
				Block.box(10, 11, 10, 11, 15, 11),
				Block.box(14, 11, 8, 15, 15, 9)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.WEST,
		Stream.of(
				Block.box(3, 0, 0, 16, 10, 16),
				Block.box(11, 10, 8, 16, 16, 16),
				Block.box(11, 10, 2, 15, 12, 6),
				Block.box(4, 10, 2, 8, 11, 6),
				Block.box(12, 12, 3, 14, 16, 5),
				Block.box(6, 13.5, 2, 13, 15.5, 6),
				Block.box(5, 12, 3, 7, 16, 5),
				Block.box(3, 10, 9, 4, 15, 15),
				Block.box(4, 10, 8, 11, 11, 16),
				Block.box(9, 11, 10, 10, 15, 11),
				Block.box(7, 11, 9, 8, 15, 10),
				Block.box(5, 11, 10, 6, 15, 11),
				Block.box(7, 11, 14, 8, 15, 15)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get()
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return SplicerBlock.SPLICER.get(state.getValue(SplicerBlock.FACING));
    }

    public SplicerBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SplicerBlock.FACING, Direction.NORTH).setValue(
                SplicerBlock.FIXED, false).setValue(SplicerBlock.WATERLOGGED, false));
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new SplicerTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level world, final BlockState state,
            final BlockEntityType<T> type)
    {
        return ITickTile.getTicker(world, state, type);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(SplicerBlock.FACING);
        builder.add(SplicerBlock.FIXED);
        builder.add(SplicerBlock.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(SplicerBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(SplicerBlock.FIXED, false).setValue(SplicerBlock.WATERLOGGED, flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
            final BlockPos facingPos)
    {
        if (state.getValue(SplicerBlock.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(SplicerBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

}
