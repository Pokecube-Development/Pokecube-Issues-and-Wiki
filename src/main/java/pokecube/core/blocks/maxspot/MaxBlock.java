package pokecube.core.blocks.maxspot;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.MaterialColor;
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
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableHorizontalBlock;

import java.util.Objects;

public class MaxBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    protected static final DirectionProperty        FACING      = HorizontalBlock.FACING;
    protected static final BooleanProperty          WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    private static final VoxelShape DYNAMAX = VoxelShapes.or(
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(3, 3, 3, 13, 14, 13)).optimize();
    private static final VoxelShape DYNAMAX_COLLISION = VoxelShapes.or(
            Block.box(2, 0, 2, 14, 3, 14)).optimize();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
                               final ISelectionContext context)
    {
        return DYNAMAX;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
                               final ISelectionContext context)
    {
        return DYNAMAX_COLLISION;
    }

    public MaxBlock(final Properties properties, final MaterialColor color)
    {
        super(properties, color);
        this.registerDefaultState(this.stateDefinition.any().setValue(MaxBlock.FACING, Direction.NORTH).setValue(
                MaxBlock.WATERLOGGED, false));
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
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(MaxBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(MaxBlock.WATERLOGGED, ifluidstate.is(
                        FluidTags.WATER) && ifluidstate.getAmount() == 8);
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
