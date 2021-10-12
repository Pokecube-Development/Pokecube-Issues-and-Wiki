package pokecube.legends.blocks.containers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.tileentity.GenericBookshelfEmptyTile;

public class GenericBookshelfEmpty extends BaseEntityBlock implements SimpleWaterloggedBlock
{
    public static final IntegerProperty             BOOKS       = IntegerProperty.create("books", 0, 9);
    private static final Map<Direction, VoxelShape> EMPTY       = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box @formatter:off
    static
    {
    	GenericBookshelfEmpty.EMPTY.put(Direction.NORTH, Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(1, 7, 0, 15, 9, 16),
            Block.box(0, 1, 0, 1, 15, 16),
            Block.box(15, 1, 0, 16, 15, 16),
            Block.box(1, 1, 7, 15, 15, 9),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());
    	GenericBookshelfEmpty.EMPTY.put(Direction.EAST, Shapes.or(
		    Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 7, 1, 16, 9, 15),
            Block.box(0, 1, 15, 16, 15, 16),
            Block.box(0, 1, 0, 16, 15, 1),
            Block.box(7, 1, 1, 9, 15, 15),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());
    	GenericBookshelfEmpty.EMPTY.put(Direction.SOUTH, Shapes.or(
			Block.box(0, 0, 0, 16, 1, 16),
            Block.box(1, 7, 0, 15, 9, 16),
            Block.box(0, 1, 0, 1, 15, 16),
            Block.box(15, 1, 0, 16, 15, 16),
            Block.box(1, 1, 7, 15, 15, 9),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());
    	GenericBookshelfEmpty.EMPTY.put(Direction.WEST, Shapes.or(
			Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 7, 1, 16, 9, 15),
            Block.box(0, 1, 15, 16, 15, 16),
            Block.box(0, 1, 0, 16, 15, 1),
            Block.box(7, 1, 1, 9, 15, 15),
            Block.box(0, 15, 0, 16, 16, 16)).optimize());
    }

    // Precise selection box @formatter:on
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter world, final BlockPos pos,
            final CollisionContext context)
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
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new GenericBookshelfEmptyTile(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
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
            final LevelAccessor world, final BlockPos currentPos, final BlockPos facingPos)
    {
        if (state.getValue(GenericBookshelfEmpty.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos,
                Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        final BlockEntity tile = world.getBlockEntity(pos);
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
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(GenericBookshelfEmpty.BOOKS, GenericBookshelfEmpty.FACING, GenericBookshelfEmpty.WATERLOGGED);
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level world, final BlockPos pos)
    {
        final int books = this.getBooks(state);
        return books;
    }

    @Override
    public float getEnchantPowerBonus(final BlockState state, final LevelReader world, final BlockPos pos)
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
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos,
            final Player entity, final InteractionHand hand, final BlockHitResult hit)
    {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof GenericBookshelfEmptyTile) return ((GenericBookshelfEmptyTile) tile).interact(entity, hand, world);
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(final BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(final BlockState state, final BlockGetter world, final BlockPos pos)
    {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, final BlockGetter reader, final BlockPos pos)
    {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(final BlockState state, final Level world, final BlockPos pos, final BlockState state1,
            final boolean b)
    {
        if (!state.is(state1.getBlock()))
        {
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof GenericBookshelfEmptyTile)
            {
                Containers.dropContents(world, pos, ((GenericBookshelfEmptyTile) tile).getItems());
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, state1, b);
        }
    }
}