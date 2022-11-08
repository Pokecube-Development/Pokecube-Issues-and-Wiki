package thut.tech.common.items;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thut.lib.TComponent;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

public class ItemLinker extends Item
{
    public ItemLinker(final Item.Properties props)
    {
        super(props);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        final ItemStack stack = context.getItemInHand();
        final Player playerIn = context.getPlayer();
        final BlockPos pos = context.getClickedPos();
        final Level worldIn = context.getLevel();
        final BlockState state = worldIn.getBlockState(pos);
        final Direction face = context.getClickedFace();

        final boolean linked = stack.hasTag() && stack.getTag().contains("lift");
        if (!linked && state.getBlock() == TechCore.LIFTCONTROLLER.get())
        {
            final ControllerTile te = (ControllerTile) worldIn.getBlockEntity(pos);
            te.editFace[face.ordinal()] = !te.editFace[face.ordinal()];
            return InteractionResult.SUCCESS;
        }

        if (!stack.hasTag()) return InteractionResult.PASS;
        else
        {
            if (state.getBlock() == TechCore.LIFTCONTROLLER.get() && !playerIn.isShiftKeyDown())
            {
                final ControllerTile te = (ControllerTile) worldIn.getBlockEntity(pos);
                te.setSide(face, true);
                return InteractionResult.SUCCESS;
            }

            UUID liftID;
            try
            {
                liftID = UUID.fromString(stack.getTag().getString("lift"));
            }
            catch (final Exception e)
            {
                liftID = new UUID(0000, 0000);
            }
            final EntityLift lift = EntityLift.getLiftFromUUID(liftID, worldIn);
            if (playerIn.isShiftKeyDown() && lift != null && state.getBlock() == TechCore.LIFTCONTROLLER.get())
            {
                if (face != Direction.UP && face != Direction.DOWN)
                {
                    final ControllerTile te = (ControllerTile) worldIn.getBlockEntity(pos);
                    te.setLift(lift);
                    int floor = te.getButtonFromClick(face, context.getClickLocation().x, context.getClickLocation().y,
                            context.getClickLocation().z);
                    te.setFloor(floor);
                    if (floor >= 64) floor = 64 - floor;
                    final String message = "msg.floorSet";
                    if (!worldIn.isClientSide)
                        thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, floor));
                    return InteractionResult.SUCCESS;
                }
            }
            else if (playerIn.isShiftKeyDown() && state.getBlock() == TechCore.LIFTCONTROLLER.get()
                    && face != Direction.UP && face != Direction.DOWN)
            {
                final ControllerTile te = (ControllerTile) worldIn.getBlockEntity(pos);
                te.editFace[face.ordinal()] = !te.editFace[face.ordinal()];
                te.setSidePage(face, 0);
                final String message = "msg.editMode";
                if (!worldIn.isClientSide) thut.lib.ChatHelper.sendSystemMessage(playerIn,
                        TComponent.translatable(message, te.editFace[face.ordinal()]));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public void setLift(final EntityLift lift, final ItemStack stack)
    {
        if (stack.getTag() == null) stack.setTag(new CompoundTag());
        stack.getTag().putString("lift", lift.getStringUUID());
    }

    @Override
    public Component getName(final ItemStack stack)
    {
        if (stack.hasTag() && stack.getTag().contains("lift"))
            return TComponent.translatable("item.thuttech.linker.linked");
        return super.getName(stack);
    }

    @Override
    public boolean shouldOverrideMultiplayerNbt()
    {
        return true;
    }
}
