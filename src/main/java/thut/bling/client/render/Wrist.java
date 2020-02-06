package thut.bling.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.IModel;

public class Wrist
{
    public static void renderWrist(final MatrixStack mat, final IRenderTypeBuffer buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final ResourceLocation[] textures, final int brightness,
            final int overlay)
    {
        float s, sy, sx, sz, dx, dy, dz;
        dx = 0.f;
        dy = .06f;
        dz = 0.f;
        s = 0.475f;
        sx = 1.05f * s / 2;
        sy = s * 1.8f / 2;
        sz = s / 2;
        final Vector3f dr = new Vector3f(dx, dy, dz);
        final Vector3f ds = new Vector3f(sx, sy, sz);
        mat.rotate(net.minecraft.client.renderer.Vector3f.XP.rotationDegrees(90));
        mat.rotate(net.minecraft.client.renderer.Vector3f.ZP.rotationDegrees(180));
        Util.renderStandardModelWithGem(mat, buff, stack, "main", "gem", model, textures, dr, ds, brightness, overlay);
    }

}
