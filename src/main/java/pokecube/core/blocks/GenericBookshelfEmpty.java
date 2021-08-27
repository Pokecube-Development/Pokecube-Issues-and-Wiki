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
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.tileentity.GenericBookshelfEmptyTile;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GenericBookshelfEmpty extends ContainerBlock implements IWaterLoggable
{
    public static final IntegerProperty BOOKS = IntegerProperty.create("books", 0, 9);
    private static final Map<Direction, VoxelShape> EMPTY = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOK_1 = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOKS_2 = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOKS_3 = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOKS_4 = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOKS_5 = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOKS_6 = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOKS_7 = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOKS_8 = new HashMap<>();
    private static final Map<Direction, VoxelShape> BOOKS_9 = new HashMap<>();
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
            Block.box(7, 1, 1, 9, 15, 15),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());

        GenericBookshelfEmpty.BOOK_1.put(Direction.NORTH, VoxelShapes.or(
            Block.box(13, 9, 1, 15, 15, 7),
            Block.box(1, 9, 9, 3, 15, 15)).optimize());
        GenericBookshelfEmpty.BOOK_1.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 9, 13, 15, 15, 15),
            Block.box(1, 9, 1, 7, 15, 3)).optimize());
        GenericBookshelfEmpty.BOOK_1.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(13, 9, 1, 15, 15, 7),
            Block.box(1, 9, 9, 3, 15, 15)).optimize());
        GenericBookshelfEmpty.BOOK_1.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 9, 13, 15, 15, 15),
            Block.box(1, 9, 1, 7, 15, 3)).optimize());

        GenericBookshelfEmpty.BOOKS_2.put(Direction.NORTH, VoxelShapes.or(
            Block.box(13, 1, 1, 15, 6, 7),
            Block.box(12, 9, 2, 13, 13, 7),
            Block.box(3, 9, 9, 4, 13, 14),
            Block.box(1, 1, 9, 3, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_2.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 9, 12, 14, 13, 13),
            Block.box(9, 1, 13, 15, 6, 15),
            Block.box(2, 9, 3, 7, 13, 4),
            Block.box(1, 1, 1, 7, 6, 3)).optimize());
        GenericBookshelfEmpty.BOOKS_2.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(13, 1, 1, 15, 6, 7),
            Block.box(12, 9, 2, 13, 13, 7),
            Block.box(3, 9, 9, 4, 13, 14),
            Block.box(1, 1, 9, 3, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_2.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 9, 12, 14, 13, 13),
            Block.box(9, 1, 13, 15, 6, 15),
            Block.box(2, 9, 3, 7, 13, 4),
            Block.box(1, 1, 1, 7, 6, 3)).optimize());

        GenericBookshelfEmpty.BOOKS_3.put(Direction.NORTH, VoxelShapes.or(
            Block.box(11, 1, 1, 13, 7, 7),
            Block.box(1, 1, 1, 2, 6, 7),
            Block.box(3, 1, 9, 5, 7, 15),
            Block.box(14, 1, 9, 15, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_3.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 1, 11, 15, 7, 13),
            Block.box(9, 1, 1, 15, 6, 2),
            Block.box(1, 1, 3, 7, 7, 5),
            Block.box(1, 1, 14, 7, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_3.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(11, 1, 1, 13, 7, 7),
            Block.box(1, 1, 1, 2, 6, 7),
            Block.box(3, 1, 9, 5, 7, 15),
            Block.box(14, 1, 9, 15, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_3.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 1, 11, 15, 7, 13),
            Block.box(9, 1, 1, 15, 6, 2),
            Block.box(1, 1, 3, 7, 7, 5),
            Block.box(1, 1, 14, 7, 6, 15)).optimize());

        GenericBookshelfEmpty.BOOKS_4.put(Direction.NORTH, VoxelShapes.or(
            Block.box(1, 9, 0, 3, 15, 7),
            Block.box(2, 1, 0, 3, 7, 7),
            Block.box(13, 9, 9, 15, 15, 16),
            Block.box(13, 1, 9, 14, 7, 16)).optimize());
        GenericBookshelfEmpty.BOOKS_4.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 9, 1, 16, 15, 3),
            Block.box(9, 1, 2, 16, 7, 3),
            Block.box(0, 9, 13, 7, 15, 15),
            Block.box(0, 1, 13, 7, 7, 14)).optimize());
        GenericBookshelfEmpty.BOOKS_4.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(1, 9, 0, 3, 15, 7),
            Block.box(2, 1, 0, 3, 7, 7),
            Block.box(13, 9, 9, 15, 15, 16),
            Block.box(13, 1, 9, 14, 7, 16)).optimize());
        GenericBookshelfEmpty.BOOKS_4.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 9, 1, 16, 15, 3),
            Block.box(9, 1, 2, 16, 7, 3),
            Block.box(0, 9, 13, 7, 15, 15),
            Block.box(0, 1, 13, 7, 7, 14)).optimize());

        GenericBookshelfEmpty.BOOKS_5.put(Direction.NORTH, VoxelShapes.or(
            Block.box(3, 9, 2, 5, 13, 7),
            Block.box(9, 1, 2, 11, 5, 7),
            Block.box(11, 9, 9, 13, 13, 14),
            Block.box(5, 1, 9, 7, 5, 14)).optimize());
        GenericBookshelfEmpty.BOOKS_5.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 9, 3, 14, 13, 5),
            Block.box(9, 1, 9, 14, 5, 11),
            Block.box(2, 9, 11, 7, 13, 13),
            Block.box(2, 1, 5, 7, 5, 7)).optimize());
        GenericBookshelfEmpty.BOOKS_5.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(3, 9, 2, 5, 13, 7),
            Block.box(9, 1, 2, 11, 5, 7),
            Block.box(11, 9, 9, 13, 13, 14),
            Block.box(5, 1, 9, 7, 5, 14)).optimize());
        GenericBookshelfEmpty.BOOKS_5.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 9, 3, 14, 13, 5),
            Block.box(9, 1, 9, 14, 5, 11),
            Block.box(2, 9, 11, 7, 13, 13),
            Block.box(2, 1, 5, 7, 5, 7)).optimize());

        GenericBookshelfEmpty.BOOKS_6.put(Direction.NORTH, VoxelShapes.or(
            Block.box(10, 9, 1, 12, 14, 7),
            Block.box(3, 1, 1, 5, 6, 7),
            Block.box(4, 9, 9, 6, 14, 15),
            Block.box(11, 1, 9, 13, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_6.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 9, 10, 15, 14, 12),
            Block.box(9, 1, 3, 15, 6, 5),
            Block.box(1, 9, 4, 7, 14, 6),
            Block.box(1, 1, 11, 7, 6, 13)).optimize());
        GenericBookshelfEmpty.BOOKS_6.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(10, 9, 1, 12, 14, 7),
            Block.box(3, 1, 1, 5, 6, 7),
            Block.box(4, 9, 9, 6, 14, 15),
            Block.box(11, 1, 9, 13, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_6.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 9, 10, 15, 14, 12),
            Block.box(9, 1, 3, 15, 6, 5),
            Block.box(1, 9, 4, 7, 14, 6),
            Block.box(1, 1, 11, 7, 6, 13)).optimize());

        GenericBookshelfEmpty.BOOKS_7.put(Direction.NORTH, VoxelShapes.or(
            Block.box(7, 9, 2, 9, 13, 7),
            Block.box(5, 1, 2, 6, 5, 7),
            Block.box(7, 9, 9, 9, 13, 14),
            Block.box(10, 1, 9, 11, 5, 14)).optimize());
        GenericBookshelfEmpty.BOOKS_7.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 9, 7, 14, 13, 9),
            Block.box(9, 1, 5, 14, 5, 6),
            Block.box(2, 9, 7, 7, 13, 9),
            Block.box(2, 1, 10, 7, 5, 11)).optimize());
        GenericBookshelfEmpty.BOOKS_7.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(7, 9, 2, 9, 13, 7),
            Block.box(5, 1, 2, 6, 5, 7),
            Block.box(7, 9, 9, 9, 13, 14),
            Block.box(10, 1, 9, 11, 5, 14)).optimize());
        GenericBookshelfEmpty.BOOKS_7.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 9, 7, 14, 13, 9),
            Block.box(9, 1, 5, 14, 5, 6),
            Block.box(2, 9, 7, 7, 13, 9),
            Block.box(2, 1, 10, 7, 5, 11)).optimize());

        GenericBookshelfEmpty.BOOKS_8.put(Direction.NORTH, VoxelShapes.or(
            Block.box(5, 9, 1, 7, 14, 7),
            Block.box(8, 1, 0, 9, 7, 7),
            Block.box(9, 9, 9, 11, 14, 15),
            Block.box(7, 1, 9, 8, 7, 16)).optimize());
        GenericBookshelfEmpty.BOOKS_8.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 9, 5, 15, 14, 7),
            Block.box(9, 1, 8, 16, 7, 9),
            Block.box(1, 9, 9, 7, 14, 11),
            Block.box(0, 1, 7, 7, 7, 8)).optimize());
        GenericBookshelfEmpty.BOOKS_8.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(5, 9, 1, 7, 14, 7),
            Block.box(8, 1, 0, 9, 7, 7),
            Block.box(9, 9, 9, 11, 14, 15),
            Block.box(7, 1, 9, 8, 7, 16)).optimize());
        GenericBookshelfEmpty.BOOKS_8.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 9, 5, 15, 14, 7),
            Block.box(9, 1, 8, 16, 7, 9),
            Block.box(1, 9, 9, 7, 14, 11),
            Block.box(0, 1, 7, 7, 7, 8)).optimize());

        GenericBookshelfEmpty.BOOKS_9.put(Direction.NORTH, VoxelShapes.or(
            Block.box(9, 9, 0, 10, 15, 7),
            Block.box(6, 1, 1, 8, 6, 7),
            Block.box(6, 9, 9, 7, 15, 16),
            Block.box(8, 1, 9, 10, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_9.put(Direction.EAST, VoxelShapes.or(
            Block.box(9, 9, 9, 16, 15, 10),
            Block.box(9, 1, 6, 15, 6, 8),
            Block.box(0, 9, 6, 7, 15, 7),
            Block.box(1, 1, 8, 7, 6, 10)).optimize());
        GenericBookshelfEmpty.BOOKS_9.put(Direction.SOUTH, VoxelShapes.or(
            Block.box(9, 9, 0, 10, 15, 7),
            Block.box(6, 1, 1, 8, 6, 7),
            Block.box(6, 9, 9, 7, 15, 16),
            Block.box(8, 1, 9, 10, 6, 15)).optimize());
        GenericBookshelfEmpty.BOOKS_9.put(Direction.WEST, VoxelShapes.or(
            Block.box(9, 9, 9, 16, 15, 10),
            Block.box(9, 1, 6, 15, 6, 8),
            Block.box(0, 9, 6, 7, 15, 7),
            Block.box(1, 1, 8, 7, 6, 10)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader world, final BlockPos pos,
            final ISelectionContext context)
    {
        int i = state.getValue(GenericBookshelfEmpty.BOOKS);
        Direction direction = state.getValue(GenericBookshelfEmpty.FACING);
        VoxelShape book_1 = VoxelShapes.or(EMPTY.get(direction), BOOK_1.get(direction)).optimize();
        VoxelShape books_2 = VoxelShapes.or(book_1, BOOKS_2.get(direction)).optimize();
        VoxelShape books_3 = VoxelShapes.or(books_2, BOOKS_3.get(direction)).optimize();
        VoxelShape books_4 = VoxelShapes.or(books_3, BOOKS_4.get(direction)).optimize();
        VoxelShape books_5 = VoxelShapes.or(books_4, BOOKS_5.get(direction)).optimize();
        VoxelShape books_6 = VoxelShapes.or(books_5, BOOKS_6.get(direction)).optimize();
        VoxelShape books_7 = VoxelShapes.or(books_6, BOOKS_7.get(direction)).optimize();
        VoxelShape books_8 = VoxelShapes.or(books_7, BOOKS_8.get(direction)).optimize();
        VoxelShape books_9 = VoxelShapes.or(books_8, BOOKS_9.get(direction)).optimize();

        if (i == 1) return book_1.optimize();
        else if (i == 2) return books_2.optimize();
        else if (i == 3) return books_3.optimize();
        else if (i == 4) return books_4.optimize();
        else if (i == 5) return books_5.optimize();
        else if (i == 6) return books_6.optimize();
        else if (i == 7) return books_7.optimize();
        else if (i == 8) return books_8.optimize();
        else if (i == 9) return books_9.optimize();
        else return GenericBookshelfEmpty.EMPTY.get(direction);
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
            .setValue(GenericBookshelfEmpty.BOOKS, context.getItemInHand().getOrCreateTagElement("BlockEntityTag").getList("Items", 10).size());
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(GenericBookshelfEmpty.WATERLOGGED))
        {
            world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state,
                            @Nullable LivingEntity entity, ItemStack stack) {
        TileEntity tile = world.getBlockEntity(pos);
        if (stack.hasCustomHoverName()) {
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

    @Override
    public TileEntity newBlockEntity(IBlockReader block) {
        return new GenericBookshelfEmptyTile();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos)
    {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
    {
        return false;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState state1, boolean b)
    {
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