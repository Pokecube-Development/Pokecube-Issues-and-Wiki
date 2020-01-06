package thut.wearables.impl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class DefaultActiveWearable implements IActiveWearable
{

    public DefaultActiveWearable()
    {
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        return null;
    }

    @Override
    public void onPutOn(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

    @Override
    public void onTakeOff(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

    @Override
    public void onUpdate(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

    @Override
    public void renderWearable(EnumWearable slot, LivingEntity wearer, ItemStack stack, float partialTicks)
    {
    }

}
