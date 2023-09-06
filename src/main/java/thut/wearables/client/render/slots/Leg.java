package thut.wearables.client.render.slots;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.lib.AxisAngles;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class Leg
{
    public static void render(final PoseStack mat, final MultiBufferSource buff, final IWearable wearable,
            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
            final float partialTicks, final boolean thinArms, final int brightness, final int overlay,
            final HumanoidModel<?> theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            mat.scale(1, -1, -1);
            mat.mulPose(Axis.YP.rotationDegrees(180));
            wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
            return;
        }
        float[] offsetArr;

        if (wearer.isCrouching() && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(4 + index)) != null)
            mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);

        if (index == 0) theModel.rightLeg.translateAndRotate(mat);
        else theModel.leftLeg.translateAndRotate(mat);

        mat.translate(0.0F, 0.4375F, 0.0625F);

        if ((offsetArr = ThutWearables.config.renderOffsets.get(4 + index)) != null)
            mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);

        final boolean render = !ThutWearables.config.renderBlacklist.contains(4 + index);
        // Mirror left leg.
        if (index == 1) Utils.mirror(1, 0, 0, mat);
        if (render)
        {
            mat.scale(1, -1, -1);
            mat.mulPose(Axis.YP.rotationDegrees(180));
            wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

}
