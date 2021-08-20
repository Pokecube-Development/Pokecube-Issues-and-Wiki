package pokecube.core.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import pokecube.legends.blocks.customblocks.Rotates;
//import pokecube.legends.tileentity.GenericBookshelfEmptyTile;

import java.util.HashMap;
import java.util.Map;

public class GenericBookshelfEmpty extends Rotates implements IWaterLoggable
{
    public static final IntegerProperty BOOKS = IntegerProperty.create("books", 0, 9);
    private static final Map<Direction, VoxelShape> EMPTY = new HashMap<>();
    private static final DirectionProperty FACING = HorizontalBlock.FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
//    public static Map<String, Item> books = Maps.newHashMap();

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
//        if (books == 9)
//        {
//            return 3f;
//        } else if (books == 8)
//        {
//            return 2.667f;
//        } else if (books == 7)
//        {
//            return 2.333f;
//        } else if (books == 6)
//        {
//            return 2f;
//        } else if (books == 5)
//        {
//            return 1.667f;
//        } else if (books == 4)
//        {
//            return 1.333f;
//        } else if (books == 3)
//        {
//            return 1f;
//        } else if (books == 2)
//        {
//            return 0.667f;
//        } else if (books == 1)
//        {
//            return 0.333f;
//        }
//        return 0f;
    }

    public int getBooks(BlockState state) {
        return (Integer)state.getValue(GenericBookshelfEmpty.BOOKS);
    }

//    public IntegerProperty getBooksProperty() {
//        return GenericBookshelfEmpty.BOOKS;
//    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand,
                                BlockRayTraceResult hit)
    {
//        TileEntity tile = world.getBlockEntity(pos);
//        GenericBookshelfEmptyTile bookshelfEmptyTile = (GenericBookshelfEmptyTile)tile;
//        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());

//        Collections.sort(names);
//        for (final String name : names) {
//            if (!world.isClientSide ) {
//                if (!world.isClientSide && tile instanceof GenericBookshelfEmptyTile) {
//                    if (bookshelfEmptyTile.placeBooks(entity.abilities.instabuild ? itemStack.copy() : itemStack, i)) {
//                        world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, i + 1), 1);
//                        world.playSound((PlayerEntity) null, pos, SoundEvents.ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 1.0F, 1.0F);
////                        itemStack.shrink(1);
//                        (bookshelfEmptyTile).setBook(itemStack.copy());
//                        System.out.println("Shelved a book tile");
//                        return ActionResultType.SUCCESS;
//                    }
//
//                    return ActionResultType.CONSUME;
//                } else if ((i > 0) && entity.isShiftKeyDown()) {
//                    world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, i - 1), 1);
//                    world.playSound((PlayerEntity) null, pos, SoundEvents.ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 1.0F, 1.0F);
////                    entity.addItem(itemStack1);
//                    this.dropBook(world, pos);
//                    books.remove(name, book);
//                    System.out.println("Removed a book");
//                    return ActionResultType.SUCCESS;
//                }
        ItemStack itemStack = entity.getItemInHand(hand);
        Item book = itemStack.getItem();
        boolean flag = book == Items.BOOK;
        int i = this.getBooks(state);

        ItemStack itemStack1 = new ItemStack(Items.BOOK);

                if (flag && !entity.isShiftKeyDown() && i != 9)
                {
                    world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, i + 1), 1);
                    world.playSound((PlayerEntity)null, pos, SoundEvents.ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    itemStack.shrink(1);
                    System.out.println("Shelved a book");
                    return ActionResultType.SUCCESS;
                }
                else if ((i > 0) && entity.isShiftKeyDown()) {
                    world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, i - 1), 1);
                    world.playSound((PlayerEntity)null, pos, SoundEvents.ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    entity.addItem(itemStack1);
                    System.out.println("Removed a book");
                    return ActionResultType.SUCCESS;
                }
        return ActionResultType.PASS;
    }

//    public void dropBook(World world, BlockPos pos) {
//        if (!world.isClientSide) {
//            TileEntity tile = world.getBlockEntity(pos);
//            if (tile instanceof GenericBookshelfEmptyTile) {
//                GenericBookshelfEmptyTile bookshelfEmptyTile = (GenericBookshelfEmptyTile)tile;
//                ItemStack itemStack = bookshelfEmptyTile.getBooks();
//                if (!itemStack.isEmpty()) {
//                    world.levelEvent(1010, pos, 0);
//                    bookshelfEmptyTile.clearContent();
//                    float w = 0.7F;
//                    double x = (double)(world.random.nextFloat() * 0.7F) + 0.15000000596046448D;
//                    double y = (double)(world.random.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
//                    double z = (double)(world.random.nextFloat() * 0.7F) + 0.15000000596046448D;
//                    ItemStack itemStack1 = itemStack.copy();
//                    ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + x, (double)pos.getY() + y, (double)pos.getZ() + z, itemStack1);
//                    itemEntity.setDefaultPickUpDelay();
//                    world.addFreshEntity(itemEntity);
//                }
//            }
//        }
//    }

//    public TileEntity newBlockEntity(IBlockReader block) {
//        return new GenericBookshelfEmptyTile();
//    }

//    @Override
//    public void onRemove(BlockState state, World world, BlockPos pos, BlockState state1, boolean b) {
//        if (!state.is(state1.getBlock())) {
//            TileEntity tile = world.getBlockEntity(pos);
//            if (tile instanceof GenericBookshelfEmptyTile) {
//                InventoryHelper.dropContents(world, pos, ((GenericBookshelfEmptyTile)tile).getItems());
//            }
//
//            super.onRemove(state, world, pos, state1, b);
//        }
//
//    }
}