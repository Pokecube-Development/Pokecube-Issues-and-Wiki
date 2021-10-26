package thut.wearables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

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
            final int subIndex, final ItemUseContext context)
    {
        if (!itemstack.isEmpty())
        {
            final ActionResultType result = itemstack.getItem().useOn(context);
            if (result == ActionResultType.PASS && player instanceof PlayerEntity) itemstack.use(player
                    .getCommandSenderWorld(), (PlayerEntity) player, Hand.MAIN_HAND);
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
        else if (player instanceof PlayerEntity) itemstack.getItem().onArmorTick(itemstack, player.getCommandSenderWorld(),
                (PlayerEntity) player);
        else itemstack.getItem().inventoryTick(itemstack, player.getCommandSenderWorld(), player, slot.index + subIndex,
                false);
    }
}
