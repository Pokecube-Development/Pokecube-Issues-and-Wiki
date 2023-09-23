package thut.tech.common.blocks.lift;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import thut.api.block.ITickTile;
import thut.core.common.network.TileUpdate;
import thut.lib.TComponent;
import thut.tech.common.TechCore;

public class ControllerBlock extends Block implements EntityBlock
{
    public static final BooleanProperty CALLED = BooleanProperty.create("called");
    public static final BooleanProperty MASKED = BooleanProperty.create("masked");
    public static final BooleanProperty CURRENT = BooleanProperty.create("current");

    public ControllerBlock(final Block.Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(ControllerBlock.CALLED, false)
                .setValue(ControllerBlock.MASKED, false).setValue(ControllerBlock.CURRENT, false));
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new ControllerTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
            final BlockEntityType<T> type)
    {
        return ITickTile.getTicker(level, state, type);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(ControllerBlock.CALLED);
        builder.add(ControllerBlock.CURRENT);
        builder.add(ControllerBlock.MASKED);
    }

    // RedStone stuff
    @Override
    public int getSignal(final BlockState blockState, final BlockGetter blockAccess, final BlockPos pos, Direction side)
    {
        final ControllerTile te = (ControllerTile) blockAccess.getBlockEntity(pos);
        final boolean here = blockState.getValue(ControllerBlock.CURRENT);
        final boolean called = blockState.getValue(ControllerBlock.CALLED);
        side = side.getOpposite();
        if (te.isSideOn(side))
        {
            if (te.isCallPanel(side)) return called ? 15 : 0;
            if (te.isFloorDisplay(side)) return here ? 15 : 0;
            return here ? 5 : 0;
        }
        return 0;
    }

    @Override
    public int getDirectSignal(final BlockState blockState, final BlockGetter blockAccess, final BlockPos pos,
            Direction side)
    {
        final ControllerTile te = (ControllerTile) blockAccess.getBlockEntity(pos);
        final boolean here = blockState.getValue(ControllerBlock.CURRENT);
        final boolean called = blockState.getValue(ControllerBlock.CALLED);
        side = side.getOpposite();
        if (te.isSideOn(side))
        {
            if (te.isCallPanel(side)) return called ? 15 : 0;
            if (te.isFloorDisplay(side)) return here ? 15 : 0;
        }
        return 0;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side)
    {
        final ControllerTile te = (ControllerTile) level.getBlockEntity(pos);
        side = side.getOpposite();
        final boolean called = state.getValue(ControllerBlock.CALLED);
        // Note that we do not check if the side is on, as this allows
        // only buttons, with no display number!
        if (te.isCallPanel(side) && !called) return true;
        return false;
    }

    /**
     * Can this block provide power. Only wire currently seems to have this
     * change based on its state.
     */
    @Override
    public boolean isSignalSource(final BlockState state)
    {
        return true;
    }

    // End of Redstone

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos, final Player playerIn,
            final InteractionHand handIn, final BlockHitResult hit)
    {
        final ItemStack heldItem = playerIn.getItemInHand(handIn);
        final Direction side = hit.getDirection();
        final boolean linkerOrStick = heldItem.getItem() == Items.STICK || heldItem.getItem() == TechCore.LINKER.get();
        var be = worldIn.getBlockEntity(pos);
        if (!(be instanceof ControllerTile te)) return InteractionResult.PASS;
        // This happens when sent from client side!
        if (!be.hasLevel()) be.setLevel(worldIn);
        if (linkerOrStick && playerIn.isShiftKeyDown())
        {
            if (te.isSideOn(side))
            {
                te.setSide(side, false);
                if (!te.getLevel().isClientSide) TileUpdate.sendUpdate(te);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (!linkerOrStick && side == Direction.DOWN)
        {
            if (heldItem.getItem() instanceof BlockItem block)
            {
                final BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(playerIn, handIn, hit));
                te.copiedState = block.getBlock().getStateForPlacement(context);
                worldIn.setBlockAndUpdate(pos, state.setValue(ControllerBlock.MASKED, true));
                if (!te.getLevel().isClientSide) TileUpdate.sendUpdate(te);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (!te.isSideOn(side) || heldItem.getItem() == Items.STICK)
        {
            if (linkerOrStick)
            {
                if (!worldIn.isClientSide)
                {
                    te.setSide(side, !te.isSideOn(side));
                    if (playerIn instanceof ServerPlayer player) te.sendUpdate(player);
                }
                return InteractionResult.SUCCESS;
            }
        }
        else if (te.isSideOn(side)) if (heldItem.getItem() == TechCore.LINKER.get())
        {
            if (!worldIn.isClientSide && !te.isEditMode(side) && !te.isFloorDisplay(side))
            {
                te.setSidePage(side, (te.getSidePage(side) + 1) % 8);
                if (playerIn instanceof ServerPlayer player) te.sendUpdate(player);
                TileUpdate.sendUpdate(te);
            }
            return InteractionResult.SUCCESS;
        }
        else if (!playerIn.isShiftKeyDown())
        {
            final float hitX = (float) hit.getLocation().x;
            final float hitY = (float) hit.getLocation().y;
            final float hitZ = (float) hit.getLocation().z;
            return te.doButtonClick(playerIn, side, hitX, hitY, hitZ) ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }
        if (playerIn.isShiftKeyDown() && handIn == InteractionHand.MAIN_HAND && playerIn instanceof ServerPlayer)
        {
            final boolean sideOn = !te.isSideOn(side);
            thut.lib.ChatHelper.sendSystemMessage(playerIn,
                    TComponent.translatable("msg.lift.side." + (sideOn ? "on" : "off")));
            if (sideOn)
            {
                final boolean call = te.isCallPanel(side);
                final boolean edit = te.isEditMode(side);
                final boolean display = te.isFloorDisplay(side);
                if (edit)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable("msg.lift.side.edit"));
                else if (call)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable("msg.lift.side.call"));
                else if (display)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable("msg.lift.side.display"));
                else
                {
                    final int page = te.getSidePage(side);
                    thut.lib.ChatHelper.sendSystemMessage(playerIn,
                            TComponent.translatable("msg.lift.side.page", page));
                }
            }
        }
        return InteractionResult.PASS;
    }
}
