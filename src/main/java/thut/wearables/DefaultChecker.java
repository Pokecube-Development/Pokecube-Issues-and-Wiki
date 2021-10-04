package thut.wearables;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class DefaultChecker implements IWearableChecker
{
    @Override
    public boolean canRemove(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
        if (itemstack.isEmpty()) return true;
        IActiveWearable wearable;
        if ((wearable = itemstack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null) return wearable
                .canRemove(player, itemstack, slot, subIndex);
        if (itemstack.getItem() instanceof IActiveWearable) return ((IActiveWearable) itemstack.getItem()).canRemove(
                player, itemstack, slot, subIndex);
        return true;
    }

    @Override
    public void onInteract(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex, final UseOnContext context)
    {
        if (!itemstack.isEmpty())
        {
            final InteractionResult result = itemstack.getItem().useOn(context);
            if (result == InteractionResult.PASS && player instanceof Player) itemstack.use(player
                    .getCommandSenderWorld(), (Player) player, InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void onPutOn(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
        if (itemstack.isEmpty()) return;
        IActiveWearable wearable;
        if ((wearable = itemstack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null)
        {
            wearable.onPutOn(player, itemstack, slot, subIndex);
            return;
        }
        if (itemstack.getItem() instanceof IActiveWearable) ((IActiveWearable) itemstack.getItem()).onPutOn(player,
                itemstack, slot, subIndex);
    }

    @Override
    public void onTakeOff(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
        if (itemstack.isEmpty()) return;
        IActiveWearable wearable;
        if ((wearable = itemstack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null)
        {
            wearable.onTakeOff(player, itemstack, slot, subIndex);
            return;
        }
        if (itemstack.getItem() instanceof IActiveWearable) ((IActiveWearable) itemstack.getItem()).onTakeOff(player,
                itemstack, slot, subIndex);
    }

    @Override
    public void onUpdate(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
        if (itemstack == null) return;
        IActiveWearable wearable;
        if ((wearable = itemstack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null) wearable
                .onUpdate(player, itemstack, slot, subIndex);
        if (itemstack.getItem() instanceof IActiveWearable) ((IActiveWearable) itemstack.getItem()).onUpdate(player,
                itemstack, slot, subIndex);
        else if (player instanceof Player) itemstack.getItem().onArmorTick(itemstack, player.getCommandSenderWorld(),
                (Player) player);
        else itemstack.getItem().inventoryTick(itemstack, player.getCommandSenderWorld(), player, slot.index + subIndex,
                false);
    }
}
