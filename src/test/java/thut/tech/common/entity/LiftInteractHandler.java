package thut.tech.common.entity;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.core.common.ThutCore;
import thut.tech.common.TechCore;

public class LiftInteractHandler extends BlockEntityInteractHandler
{
    public static boolean DROPSPARTS = true;

    final EntityLift lift;

    public LiftInteractHandler(final EntityLift lift)
    {
        super(lift);
        this.lift = lift;
    }

    @Override
    public InteractionResult interactInternal(final Player player, final BlockPos pos, final ItemStack stack,
            final InteractionHand hand)
    {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult processInitialInteract(final Player player, @Nullable ItemStack stack,
            final InteractionHand hand)
    {
        final boolean isElevatorItemOrStick = stack.getItem() == Items.STICK || stack.getItem() == TechCore.LIFT.get();
        final boolean isLinker = stack.getItem() == TechCore.LINKER.get();

        final boolean canEdit = player.getUUID().equals(this.lift.owner) || player.getAbilities().instabuild;

        final boolean shouldLinkLift = player.isShiftKeyDown() && isLinker && canEdit;
        final boolean shouldKillLiftUnowned = this.lift.owner == null;
        final boolean shouldDisplayOwner = isLinker && canEdit;
        final boolean shouldKillLiftOwned = player.isShiftKeyDown() && isElevatorItemOrStick && canEdit;

        if (shouldKillLiftUnowned)
        {
            ThutCore.LOGGER.error("Killing unowned Lift: " + this.lift);
            if (!this.lift.getCommandSenderWorld().isClientSide)
            {
                final String message = "msg.lift.killed";
                player.sendMessage(new TranslatableComponent(message), Util.NIL_UUID);
                if (LiftInteractHandler.DROPSPARTS)
                {
                    final BlockPos max = this.lift.boundMax;
                    final BlockPos min = this.lift.boundMin;
                    final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
                    final int num = (dw + 1) * (max.getY() - min.getY() + 1);
                    stack = new ItemStack(TechCore.LIFT.get());
                    stack.setCount(num);
                    player.drop(stack, false, true);
                }
                this.lift.remove(RemovalReason.KILLED);
            }
            return InteractionResult.SUCCESS;
        }
        else if (shouldLinkLift)
        {
            if (stack.getTag() == null) stack.setTag(new CompoundTag());
            stack.getTag().putString("lift", this.lift.getStringUUID());

            final String message = "msg.liftSet";

            if (!this.lift.getCommandSenderWorld().isClientSide) player.sendMessage(new TranslatableComponent(message),
                    Util.NIL_UUID);
            return InteractionResult.SUCCESS;
        }
        else if (shouldDisplayOwner)
        {
            if (!this.lift.getCommandSenderWorld().isClientSide && this.lift.owner != null)
            {
                final Entity ownerentity = this.lift.getCommandSenderWorld().getPlayerByUUID(this.lift.owner);
                final String message = "msg.lift.owner";

                player.sendMessage(new TranslatableComponent(message, ownerentity.getName()), Util.NIL_UUID);
            }
            return InteractionResult.SUCCESS;
        }
        else if (shouldKillLiftOwned)
        {
            if (!this.lift.getCommandSenderWorld().isClientSide)
            {
                final String message = "msg.lift.killed";
                player.sendMessage(new TranslatableComponent(message), Util.NIL_UUID);
                if (LiftInteractHandler.DROPSPARTS)
                {
                    final BlockPos max = this.lift.boundMax;
                    final BlockPos min = this.lift.boundMin;
                    final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
                    final int num = (dw + 1) * (max.getY() - min.getY() + 1);
                    stack = new ItemStack(TechCore.LIFT.get());
                    stack.setCount(num);
                    player.drop(stack, false, true);
                }
                this.lift.remove(RemovalReason.KILLED);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
