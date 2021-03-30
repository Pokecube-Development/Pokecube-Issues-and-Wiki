package pokecube.core.blocks.pc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import pokecube.core.network.packets.PacketPC;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PCBlock extends HorizontalBlock implements IWaterLoggable
{

    private static final Map<Direction, VoxelShape> PC_TOP      = new HashMap<>();
    private static final Map<Direction, VoxelShape> PC_BASE     = new HashMap<>();
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    final boolean                         top;
    final boolean                         needsBase;

    // Precise selection box
    static
    {
        PC_TOP.put(Direction.NORTH,
                Block.box(0, 0, 8, 16, 16, 16));
        PC_TOP.put(Direction.EAST,
                Block.box(0, 0, 0, 8, 16, 16));
        PC_TOP.put(Direction.SOUTH,
                Block.box(0, 0, 0, 16, 16, 8));
        PC_TOP.put(Direction.WEST,
                Block.box(8, 0, 0, 16, 16, 16));
        PCBlock.PC_BASE.put(Direction.NORTH, VoxelShapes.or(
                Block.box(0, 0, 8, 16, 16, 16),
                Block.box(1, 0, 4, 15, 16, 8)).optimize());
        PCBlock.PC_BASE.put(Direction.EAST, VoxelShapes.or(
                Block.box(0, 0, 0, 8, 16, 16),
                Block.box(8, 0, 1, 12, 16, 15)).optimize());
        PCBlock.PC_BASE.put(Direction.SOUTH, VoxelShapes.or(
                Block.box(0, 0, 0, 16, 16, 8),
                Block.box(1, 0, 8, 15, 16, 12)).optimize());
        PCBlock.PC_BASE.put(Direction.WEST, VoxelShapes.or(
                Block.box(8, 0, 0, 16, 16, 16),
                Block.box(4, 0, 1, 8, 16, 15)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        if (this.top) return PCBlock.PC_TOP.get(state.getValue(PCBlock.FACING));
        else return PCBlock.PC_BASE.get(state.getValue(PCBlock.FACING));
    }

    public PCBlock(final Properties properties, final boolean top)
    {
        this(properties, top, true);
    }

    // Default States
    public PCBlock(final Properties properties, final boolean top, final boolean needsBase)
    {
        super(properties);
        this.top = top;
        this.needsBase = needsBase;
        this.registerDefaultState(this.stateDefinition.any().setValue(PCBlock.FACING, Direction.NORTH).setValue(
                PCBlock.WATERLOGGED, false));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new PCTile();
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(PCBlock.FACING, PCBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(PCBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(PCBlock.WATERLOGGED, ifluidstate.is(
                        FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(PCBlock.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        if (this.top && (!this.needsBase || world.getBlockState(pos.below()).getBlock() instanceof PCBlock))
        {
            if (player instanceof ServerPlayerEntity) PacketPC.sendOpenPacket(player, player.getUUID(), pos);
            return ActionResultType.SUCCESS;
        }
        else return ActionResultType.PASS;
    }

    // Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(PCBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
