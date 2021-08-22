package pokecube.core.blocks;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import pokecube.legends.tileentity.GenericBookshelfEmptyTile;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GenericBookshelfEmpty extends ContainerBlock implements IWaterLoggable
{
    public static final IntegerProperty BOOKS = IntegerProperty.create("books", 0, 9);
    private static final Map<Direction, VoxelShape> EMPTY = new HashMap<>();
    private static final DirectionProperty FACING = HorizontalBlock.FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	GenericBookshelfEmpty.EMPTY.put(Direction.NORTH, VoxelShapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(1, 7, 0, 15, 9, 16),
            Block.box(0, 1, 0, 1, 15, 16),
            Block.box(15, 1, 0, 16, 15, 16),
            Block.box(1, 1, 7, 15, 15, 9),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());
    	GenericBookshelfEmpty.EMPTY.put(Direction.EAST, VoxelShapes.or(
		    Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 7, 1, 16, 9, 15),
            Block.box(0, 1, 15, 16, 15, 16),
            Block.box(0, 1, 0, 16, 15, 1),
            Block.box(7, 1, 1, 9, 15, 15),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());
    	GenericBookshelfEmpty.EMPTY.put(Direction.SOUTH, VoxelShapes.or(
			Block.box(0, 0, 0, 16, 1, 16),
            Block.box(1, 7, 0, 15, 9, 16),
            Block.box(0, 1, 0, 1, 15, 16),
            Block.box(15, 1, 0, 16, 15, 16),
            Block.box(1, 1, 7, 15, 15, 9),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());
    	GenericBookshelfEmpty.EMPTY.put(Direction.WEST, VoxelShapes.or(
			Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 7, 1, 16, 9, 15),
            Block.box(0, 1, 15, 16, 15, 16),
            Block.box(0, 1, 0, 16, 15, 1),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return GenericBookshelfEmpty.EMPTY.get(state.getValue(GenericBookshelfEmpty.FACING));
    }

    public GenericBookshelfEmpty(final Properties props)
    {
    	super(props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(GenericBookshelfEmpty.FACING, Direction.NORTH).setValue(
    			GenericBookshelfEmpty.WATERLOGGED, false).setValue(GenericBookshelfEmpty.BOOKS, 0));
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(GenericBookshelfEmpty.FACING, context.getHorizontalDirection().getOpposite())
            .setValue(GenericBookshelfEmpty.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate.getAmount() == 8)
            .setValue(GenericBookshelfEmpty.BOOKS, 0);
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state,
                            @Nullable LivingEntity entity, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof GenericBookshelfEmptyTile) {
                ((GenericBookshelfEmptyTile) tile).setCustomName(stack.getHoverName());
            }
        }
    }

    // Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(GenericBookshelfEmpty.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Deprecated
    @Override
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.setValue(GenericBookshelfEmpty.FACING, rot.rotate(state.getValue(GenericBookshelfEmpty.FACING)));
    }

    @Deprecated
    @Override
    public BlockState mirror(final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(GenericBookshelfEmpty.FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(GenericBookshelfEmpty.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(GenericBookshelfEmpty.BOOKS, GenericBookshelfEmpty.FACING, GenericBookshelfEmpty.WATERLOGGED);
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
        int books = this.getBooks(world.getBlockState(pos));
        return books;
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos)
    {
        int books = this.getBooks(world.getBlockState(pos));
        return books/3f;
    }

    public int getBooks(BlockState state) {
        return (Integer)state.getValue(GenericBookshelfEmpty.BOOKS);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand,
                                BlockRayTraceResult hit)
    {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof GenericBookshelfEmptyTile)
        {
            return ((GenericBookshelfEmptyTile) tile).interact(entity, hand, state, pos, world);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    public TileEntity newBlockEntity(IBlockReader block) {
        return new GenericBookshelfEmptyTile();
    }

    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState state1, boolean b) {
        if (!state.is(state1.getBlock())) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof GenericBookshelfEmptyTile) {
                InventoryHelper.dropContents(world, pos, ((GenericBookshelfEmptyTile)tile).getItems());
                world.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, world, pos, state1, b);
        }

    }
}