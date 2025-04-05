package thut.bling.client.render;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.parts.Material;

public class Finger
{

    public static void renderFinger(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay)
    {
        renderFinger(mat, buff, wearer, stack, model, brightness, overlay, material -> false);
    }

    public static void renderFinger(PoseStack mat, MultiBufferSource buff, LivingEntity wearer, ItemStack stack,
            IModel model, int brightness, int overlay, Predicate<Material> notColourable)
    {
        Util.renderModel(mat, buff, stack, model, brightness, overlay, notColourable);
    }
}
