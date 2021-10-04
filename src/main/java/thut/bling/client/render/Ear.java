package thut.bling.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.IModel;

public class Ear
{

    public static void renderEar(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final ResourceLocation[] textures, final int brightness,
            final int overlay)
    {
        if (!model.isLoaded() || !model.isValid()) return;
        float s, dx, dy, dz;
        dx = 0.0f;
        dy = .175f;
        dz = 0.0f;
        s = 0.475f / 4f;
        final Vector3f dr = new Vector3f(dx, dy, dz);
        final Vector3f ds = new Vector3f(s, s, s);
        mat.mulPose(com.mojang.math.Vector3f.XP.rotationDegrees(90));
        mat.mulPose(com.mojang.math.Vector3f.ZP.rotationDegrees(180));
        Util.renderStandardModelWithGem(mat, buff, stack, "main", "gem", model, textures, dr, ds, brightness, overlay);
    }
}
