package thut.wearables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface IActiveWearable extends IWearable
{
    public static class Default implements IActiveWearable
    {

        @Override
        public EnumWearable getSlot(final ItemStack stack)
        {
            return null;
        }

        @Override
        public void onPutOn(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                final int subIndex)
        {
        }

        @Override
        public void onTakeOff(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                final int subIndex)
        {
        }

        @Override
        public void onUpdate(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                final int subIndex)
        {
        }

        @Override
        public void renderWearable(final EnumWearable slot, final int subindex, final LivingEntity wearer,
                final ItemStack stack, final float partialTicks)
        {
        }

    }

    default void onPutOn(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
    }

    default void onTakeOff(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
    }

    default void onUpdate(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
    }
}
