package pokecube.core.blocks.pc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.network.packets.PacketPC;

public class PCBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock
{

    private static final Map<Direction, VoxelShape> PC_TOP      = new HashMap<>();
    private static final Map<Direction, VoxelShape> PC_BASE     = new HashMap<>();
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
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
        PCBlock.PC_BASE.put(Direction.NORTH, Shapes.or(
                Block.box(0, 0, 8, 16, 16, 16),
                Block.box(1, 0, 4, 15, 16, 8)).optimize());
        PCBlock.PC_BASE.put(Direction.EAST, Shapes.or(
                Block.box(0, 0, 0, 8, 16, 16),
                Block.box(8, 0, 1, 12, 16, 15)).optimize());
        PCBlock.PC_BASE.put(Direction.SOUTH, Shapes.or(
                Block.box(0, 0, 0, 16, 16, 8),
                Block.box(1, 0, 8, 15, 16, 12)).optimize());
        PCBlock.PC_BASE.put(Direction.WEST, Shapes.or(
                Block.box(8, 0, 0, 16, 16, 16),
                Block.box(4, 0, 1, 8, 16, 15)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
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
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
    {
        return new PCTile();
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(PCBlock.FACING, PCBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(PCBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(PCBlock.WATERLOGGED, ifluidstate.is(
                        FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
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
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos,
            final Player player, final InteractionHand hand, final BlockHitResult hit)
    {
        if (this.top && (!this.needsBase || world.getBlockState(pos.below()).getBlock() instanceof PCBlock))
        {
            if (player instanceof ServerPlayer) PacketPC.sendOpenPacket(player, player.getUUID(), pos);
            return InteractionResult.SUCCESS;
        }
        else if (this.top && (this.needsBase || !(world.getBlockState(pos.below()).getBlock() instanceof PCBlock)))
        {
            player.displayClientMessage(new TranslatableComponent("msg.pokecube.pc_top.fail"), true);
            return InteractionResult.PASS;
        }
        else if (!this.top && !(world.getBlockState(pos.above()).getBlock() instanceof PCBlock))
        {
            player.displayClientMessage(new TranslatableComponent("msg.pokecube.pc_base.fail"), true);
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    // Adds Waterlogging
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(PCBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
