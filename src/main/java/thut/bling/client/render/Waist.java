package thut.bling.client.render;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.IModel;

public class Waist
{
    public static void renderWaist(final LivingEntity wearer, final ItemStack stack, final IModel model,
            final ResourceLocation[] textures, final int brightness)
    {
        float s, dx, dy, dz;
        dx = 0;
        dy = -.0f;
        dz = -0.6f;
        s = 0.525f;
        if (wearer.getItemStackFromSlot(EquipmentSlotType.LEGS) == null) s = 0.465f;
        final Vector3f dr = new Vector3f(dx, dy, dz);
        final Vector3f ds = new Vector3f(s, s, s);
        Util.renderStandardModelWithGem(stack, "main", "gem", model, textures, brightness, dr, ds);
    }
}
