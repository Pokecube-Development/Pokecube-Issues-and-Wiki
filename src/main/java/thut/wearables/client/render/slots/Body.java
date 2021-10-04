package thut.wearables.client.render.slots;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class Body
{
    public static void render(final PoseStack mat, final MultiBufferSource buff, final IWearable wearable,
            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
            final float partialTicks, final boolean thinArms, final int brightness, final int overlay,
            final HumanoidModel<?> theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
            return;
        }
        float[] offsetArr;
        boolean render = false;

        mat.pushPose();
        switch (slot)
        {
        case BACK:
            if (ThutWearables.config.renderBlacklist.contains(7)) break;
            if (wearer.isCrouching() && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(7)) != null) mat
                    .translate(offsetArr[0], offsetArr[1], offsetArr[2]);

            theModel.body.translateAndRotate(mat);
            if ((offsetArr = ThutWearables.config.renderOffsets.get(7)) != null) mat.translate(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            render = true;
            break;
        case NECK:
            if (ThutWearables.config.renderBlacklist.contains(6)) break;
            if (wearer.isCrouching() && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(6)) != null) mat
                    .translate(offsetArr[0], offsetArr[1], offsetArr[2]);

            theModel.body.translateAndRotate(mat);
            if ((offsetArr = ThutWearables.config.renderOffsets.get(6)) != null) mat.translate(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            render = true;
            break;
        case WAIST:
            if (ThutWearables.config.renderBlacklist.contains(8)) break;
            theModel.body.translateAndRotate(mat);
            if (wearer.isCrouching() && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(8)) != null) mat
                    .translate(offsetArr[0], offsetArr[1], offsetArr[2]);

            if ((offsetArr = ThutWearables.config.renderOffsets.get(8)) != null) mat.translate(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            render = true;
            break;
        default:
            break;

        }
        if (render) wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);

        mat.popPose();
    }
}
