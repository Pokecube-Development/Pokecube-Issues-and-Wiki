package thut.wearables;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        @OnlyIn(value = Dist.CLIENT)
        public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks, int brightness, int overlay)
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
