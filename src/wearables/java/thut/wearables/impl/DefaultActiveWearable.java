package thut.wearables.impl;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
            final int brightness, final int overlay)
    {
    }
}
