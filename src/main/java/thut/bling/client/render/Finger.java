package thut.bling.client.render;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.IModel;

public class Finger
{

    public static void renderFinger(final LivingEntity wearer, final ItemStack stack, final IModel model,
            final ResourceLocation[] textures, final int brightness)
    {
        float s, dx, dy, dz;
        dx = 0.0f;
        dy = .175f;
        dz = 0.0f;
        s = 0.475f / 4f;
        final Vector3f dr = new Vector3f(dx, dy, dz);
        final Vector3f ds = new Vector3f(s, s, s);
        Util.renderStandardModelWithGem(stack, "main", "gem", model, textures, brightness, dr, ds);
    }
}
