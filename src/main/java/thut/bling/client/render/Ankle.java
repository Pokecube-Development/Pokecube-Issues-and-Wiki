package thut.bling.client.render;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.IModel;

public class Ankle
{
    public static void renderAnkle(final LivingEntity wearer, final ItemStack stack, final IModel model,
            final ResourceLocation[] textures, final int brightness)
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
        Util.renderStandardModelWithGem(stack, "main", "gem", model, textures, brightness, dr, ds);
    }

}
