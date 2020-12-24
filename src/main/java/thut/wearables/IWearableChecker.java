package thut.wearables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;

public interface IWearableChecker
{
    public boolean canRemove(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    default EnumWearable getSlot(final ItemStack stack)
    {
        if (stack.isEmpty()) return null;
        IActiveWearable wearable;
        if ((wearable = stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null) return wearable
                .getSlot(stack);
        if (stack.getItem() instanceof IWearable) return ((IWearable) stack.getItem()).getSlot(stack);
        return null;
    }

    public void onInteract(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex,
            ItemUseContext context);

    public void onPutOn(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onTakeOff(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onUpdate(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);
}
