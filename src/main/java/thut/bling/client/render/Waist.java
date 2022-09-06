package thut.bling.client.render;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.parts.Material;

public class Waist
{
    public static void renderWaist(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay)
    {
        renderWaist(mat, buff, wearer, stack, model, brightness, overlay, m -> false);
    }

    public static void renderWaist(PoseStack mat, MultiBufferSource buff, LivingEntity wearer, ItemStack stack,
            IModel model, int brightness, int overlay, Predicate<Material> notColourable)
    {

        if (!wearer.getItemBySlot(EquipmentSlot.LEGS).isEmpty())
        {
            mat.translate(0, -0.55, 0);
            mat.scale(1.15f, 1.15f, 1.15f);
        }
        else mat.translate(0, -0.65, 0);
        Util.renderModel(mat, buff, stack, model, brightness, overlay, notColourable);
    }
}
