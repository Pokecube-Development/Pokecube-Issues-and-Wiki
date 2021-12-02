package pokecube.adventures.blocks.siphon;

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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.blocks.InteractableBlock;
import pokecube.core.blocks.InteractableHorizontalBlock;
import thut.api.block.ITickTile;

public class SiphonBlock extends InteractableHorizontalBlock implements SimpleWaterloggedBlock, EntityBlock
{
    public static final DirectionProperty FACING      = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty   FIXED       = BooleanProperty.create("fixed");
    public static final BooleanProperty   WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    private static final VoxelShape SIPHON = Shapes.or(Block.box(0, 0, 0, 16, 1, 16), Block.box(1, 1, 1, 15, 7, 15),
            Block.box(2, 7, 2, 14, 12, 14), Block.box(1, 12, 1, 15, 16, 15), Block.box(6, 1, 0, 10, 5, 16), Block.box(0,
                    1, 6, 16, 5, 10)).optimize();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return SiphonBlock.SIPHON;
    }

    public SiphonBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SiphonBlock.FACING, Direction.NORTH).setValue(
                SiphonBlock.FIXED, false).setValue(SiphonBlock.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(SiphonBlock.FACING);
        builder.add(SiphonBlock.FIXED);
        builder.add(SiphonBlock.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(SiphonBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(SiphonBlock.FIXED, false).setValue(SiphonBlock.WATERLOGGED, flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState,
            final LevelAccessor world, final BlockPos currentPos, final BlockPos facingPos)
    {
        if (state.getValue(SiphonBlock.WATERLOGGED)) world.scheduleTick(currentPos, Fluids.WATER,
                Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(SiphonBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new SiphonTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level world, final BlockState state,
            final BlockEntityType<T> type)
    {
        return ITickTile.getTicker(world, state, type);
    }

    @Override
    public VoxelShape getOcclusionShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos)
    {
        return InteractableBlock.RENDERSHAPE;
    }

}
