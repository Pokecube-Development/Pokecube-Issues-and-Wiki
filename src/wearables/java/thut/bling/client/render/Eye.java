package thut.bling.client.render;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.parts.Material;

public class Eye
{
    public static void renderEye(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay)
    {
        renderEye(mat, buff, wearer, stack, model, brightness, overlay, material -> false);
    }

    public static void renderEye(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay,
            Predicate<Material> notColurable)
    {
        mat.translate(0, -0.25, 0);
        Util.renderModel(mat, buff, stack, model, brightness, overlay, notColurable);
    }
}
