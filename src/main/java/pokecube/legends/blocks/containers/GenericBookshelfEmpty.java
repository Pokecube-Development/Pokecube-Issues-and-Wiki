package pokecube.legends.blocks.containers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.tileentity.GenericBookshelfEmptyTile;

public class GenericBookshelfEmpty extends ContainerBlock implements IWaterLoggable
{
    public static final IntegerProperty             BOOKS       = IntegerProperty.create("books", 0, 9);
    private static final Map<Direction, VoxelShape> EMPTY       = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box @formatter:off
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
    }

    // Precise selection box @formatter:on
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader world, final BlockPos pos,
            final ISelectionContext context)
    {
        final Direction direction = state.getValue(GenericBookshelfEmpty.FACING);
        return GenericBookshelfEmpty.EMPTY.get(direction);
    }

    public GenericBookshelfEmpty(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(GenericBookshelfEmpty.FACING, Direction.NORTH)
                .setValue(GenericBookshelfEmpty.WATERLOGGED, false).setValue(GenericBookshelfEmpty.BOOKS, 0));
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(GenericBookshelfEmpty.FACING, context.getHorizontalDirection()
                .getOpposite()).setValue(GenericBookshelfEmpty.WATERLOGGED, ifluidstate.is(FluidTags.WATER)
                        && ifluidstate.getAmount() == 8).setValue(GenericBookshelfEmpty.BOOKS, context.getItemInHand()
                                .getOrCreateTagElement("BlockEntityTag").getList("Items", 10).size());
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState,
            final IWorld world, final BlockPos currentPos, final BlockPos facingPos)
    {
        if (state.getValue(GenericBookshelfEmpty.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos,
                Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void setPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        final TileEntity tile = world.getBlockEntity(pos);
        if (stack.hasCustomHoverName()) if (tile instanceof GenericBookshelfEmptyTile)
            ((GenericBookshelfEmptyTile) tile).setCustomName(stack.getHoverName());
    }

    // Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(GenericBookshelfEmpty.WATERLOGGED) ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
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

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final World world, final BlockPos pos)
    {
        final int books = this.getBooks(state);
        return books;
    }

    @Override
    public float getEnchantPowerBonus(final BlockState state, final IWorldReader world, final BlockPos pos)
    {
        final int books = this.getBooks(state);
        return books / 3f;
    }

    public int getBooks(final BlockState state)
    {
        if (state.hasProperty(GenericBookshelfEmpty.BOOKS)) return state.getValue(GenericBookshelfEmpty.BOOKS);
        else return 0;
    }

    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        final TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof GenericBookshelfEmptyTile) return ((GenericBookshelfEmptyTile) tile).interact(entity, hand, world);
        return ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity newBlockEntity(final IBlockReader block)
    {
        return new GenericBookshelfEmptyTile();
    }

    @Override
    public BlockRenderType getRenderShape(final BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(final BlockState state, final IBlockReader world, final BlockPos pos)
    {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, final IBlockReader reader, final BlockPos pos)
    {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(final BlockState state, final World world, final BlockPos pos, final BlockState state1,
            final boolean b)
    {
        if (!state.is(state1.getBlock()))
        {
            final TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof GenericBookshelfEmptyTile)
            {
                InventoryHelper.dropContents(world, pos, ((GenericBookshelfEmptyTile) tile).getItems());
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, state1, b);
        }
    }
}