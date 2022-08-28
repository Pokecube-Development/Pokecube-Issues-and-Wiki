package thut.wearables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public interface IWearableChecker
{
    public boolean canRemove(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    default EnumWearable getSlot(final ItemStack stack)
    {
        if (stack.isEmpty()) return null;
        IActiveWearable wearable;
        if ((wearable = stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null)
            return wearable.getSlot(stack);
        if (stack.getItem() instanceof IWearable w) return w.getSlot(stack);
        return null;
    }

    public void onInteract(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex,
            UseOnContext context);

    public void onPutOn(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onTakeOff(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onUpdate(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);
}
