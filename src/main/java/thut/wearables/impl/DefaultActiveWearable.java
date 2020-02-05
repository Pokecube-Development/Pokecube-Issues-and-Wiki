package thut.wearables.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class DefaultActiveWearable implements IActiveWearable
{

    public DefaultActiveWearable()
    {
    }

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

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
            final int brightness, final int overlay)
    {
    }
}
