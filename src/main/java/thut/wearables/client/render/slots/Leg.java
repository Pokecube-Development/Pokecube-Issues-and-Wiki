package thut.wearables.client.render.slots;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class Leg
{
    public static void render(final MatrixStack mat, final IRenderTypeBuffer buff, final IWearable wearable,
            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
            final float partialTicks, final boolean thinArms, final int brightness, final int overlay,
            final BipedModel<?> theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
            return;
        }
        float[] offsetArr;

        mat.push();

        if (wearer.isCrouching())
        {
            mat.translate(0.0F, 0.23125F, 0.01F);
            if ((offsetArr = ThutWearables.config.renderOffsetsSneak.get(4 + index)) != null)
                mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
        }

        if (index == 0) theModel.bipedRightLeg.setAnglesAndRotation(mat);
        else theModel.bipedLeftLeg.setAnglesAndRotation(mat);

        mat.translate(0.0F, 0.4375F, 0.0625F);

        if ((offsetArr = ThutWearables.config.renderOffsets.get(4 + index)) != null)
            mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);

        final boolean render = !ThutWearables.config.renderBlacklist.contains(4 + index);
        // System.out.println(slot + " " + index + " " + render);
        // Mirror left leg.
        if (index == 1) mat.scale(-1, 1, 1);
        if (render) wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        mat.pop();
    }

}
