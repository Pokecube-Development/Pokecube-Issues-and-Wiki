package thut.bling.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.IModel;

public class Waist
{
    public static void renderWaist(final MatrixStack mat, final IRenderTypeBuffer buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final ResourceLocation[] textures, final int brightness,
            final int overlay)
    {
        if (!model.isLoaded() || !model.isValid()) return;
        float s, dx, dy, dz;
        dx = 0;
        dy = -.0f;
        dz = -0.6f;
        s = 0.525f;
        if (wearer.getItemBySlot(EquipmentSlotType.LEGS).isEmpty()) s = 0.465f;
        final Vector3f dr = new Vector3f(dx, dy, dz);
        final Vector3f ds = new Vector3f(s, s, s);
        mat.mulPose(net.minecraft.util.math.vector.Vector3f.XP.rotationDegrees(90));
        mat.mulPose(net.minecraft.util.math.vector.Vector3f.ZP.rotationDegrees(180));
        Util.renderStandardModelWithGem(mat, buff, stack, "main", "gem", model, textures, dr, ds, brightness, overlay);
    }
}
