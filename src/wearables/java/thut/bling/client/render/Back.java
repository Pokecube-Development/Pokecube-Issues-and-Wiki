package thut.bling.client.render;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.parts.Material;

public class Back
{
    public static void renderBack(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay)
    {
        renderBack(mat, buff, wearer, stack, model, brightness, overlay, m -> m.name.contains("_overlay"));
    }

    public static void renderBack(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay,
            Predicate<Material> notColurable)
    {
        Util.renderModel(mat, buff, stack, model, brightness, overlay, notColurable);
    }
}
