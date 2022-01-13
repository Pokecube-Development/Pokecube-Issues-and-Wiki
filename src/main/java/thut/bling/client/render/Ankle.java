package thut.bling.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.model.IModel;

public class Ankle
{
    public static void renderAnkle(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final ResourceLocation[] textures, final int brightness,
            final int overlay)
    {
        if (!model.isLoaded() || !model.isValid()) return;
        float s, sy, sx, sz, dx, dy, dz;
        dx = 0.f;
        dy = .06f;
        dz = 0.f;
        s = 0.475f;
        sx = 1.05f * s / 2;
        sy = s * 1.8f / 2;
        sz = s / 2;
        final Vec3f dr = new Vec3f(dx, dy, dz);
        final Vec3f ds = new Vec3f(sx, sy, sz);
        mat.mulPose(com.mojang.math.Vector3f.XP.rotationDegrees(90));
        mat.mulPose(com.mojang.math.Vector3f.ZP.rotationDegrees(180));
        Util.renderStandardModelWithGem(mat, buff, stack, "main", "gem", model, textures, dr, ds, brightness, overlay);
    }

}
