package thut.tech.common.entity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
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
        final boolean shouldKillLiftOwned = isElevatorItemOrStick && canEdit;

        if (shouldKillLiftUnowned)
        {
            ThutCore.LOGGER.error("Killing unowned Lift: " + this.lift);
            if (!this.lift.getCommandSenderWorld().isClientSide)
            {
                thut.lib.ChatHelper.sendSystemMessage(player, Component.translatable("msg.lift.killed"));
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
            // TODO use linkable instead
            CompoundTag data = stack.has(DataComponents.CUSTOM_DATA)?stack.get(DataComponents.CUSTOM_DATA).copyTag():null;
            if (data == null) data = new CompoundTag();
            data.putString("lift", this.lift.getStringUUID());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));

            if (!this.lift.getCommandSenderWorld().isClientSide)
                thut.lib.ChatHelper.sendSystemMessage(player, Component.translatable("msg.liftSet"));
            return InteractionResult.SUCCESS;
        }
        else if (shouldDisplayOwner)
        {
            if (!this.lift.getCommandSenderWorld().isClientSide && this.lift.owner != null)
            {
                final Entity ownerentity = this.lift.getCommandSenderWorld().getPlayerByUUID(this.lift.owner);
                thut.lib.ChatHelper.sendSystemMessage(player, Component.translatable("msg.lift.owner", ownerentity.getName()));
            }
            return InteractionResult.SUCCESS;
        }
        else if (shouldKillLiftOwned)
        {
            if (!this.lift.getCommandSenderWorld().isClientSide)
            {
                thut.lib.ChatHelper.sendSystemMessage(player, Component.translatable("msg.lift.killed"));
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
