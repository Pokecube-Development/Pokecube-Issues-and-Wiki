package thut.bling.client.render;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.parts.Material;

public class Hat
{
    public static void renderHat(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay)
    {
        renderHat(mat, buff, wearer, stack, model, brightness, overlay, material -> !(material.name.contains("hat2")
                || material.tex != null && material.tex.getPath().contains("hat2")));
    }

    public static void renderHat(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final int brightness, final int overlay,
            Predicate<Material> notColurable)
    {
        if (!wearer.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
        {
            mat.scale(1.22f, 1.22f, 1.22f);
        }

        Util.renderModel(mat, buff, stack, model, brightness, overlay, notColurable);
    }
}
