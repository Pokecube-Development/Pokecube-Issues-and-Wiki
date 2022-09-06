package thut.bling.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.core.client.render.model.IModel;

public class Ear
{
    public static void renderEar(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay)
    {
        mat.translate(0, 0.0, -0.15);
        Util.renderModel(mat, buff, stack, model, brightness, overlay);
    }
}
