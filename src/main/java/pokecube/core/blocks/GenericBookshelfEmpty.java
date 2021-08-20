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

import java.util.HashMap;
import java.util.Map;

public class GenericBookshelfEmpty extends Rotates implements IWaterLoggable
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
}