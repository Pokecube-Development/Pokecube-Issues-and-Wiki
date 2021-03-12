package thut.wearables.client.render.slots;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class Head
{
    public static void preOffset(final MatrixStack mat, final boolean childModel, final boolean sneaking)
    {
        float[] offsetArr = new float[3];
        if (sneaking && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(9)) != null) mat.translate(
                offsetArr[0], offsetArr[1], offsetArr[2]);
        if (childModel)
        {
            final float af = 2.0F;
            final float af1 = 1.4F;
            mat.translate(0.0F, 0.5F, 0.0F);
            mat.scale(af1 / af, af1 / af, af1 / af);
            mat.translate(0.0F, 16.0F, 0.0F);
        }
    }

    public static boolean postOffset(final MatrixStack mat, final int index, final EnumWearable slot)
    {
        float[] offsetArr = new float[3];
        mat.translate(0, -0.25f, 0);
        boolean render = false;
        switch (slot)
        {
        case EAR:
            if (index == 0)
            {
                if (ThutWearables.config.renderBlacklist.contains(9)) break;
                mat.translate(-0.25, -0.1, 0.0);
                mat.mulPose(Vector3f.YP.rotationDegrees(90));
                mat.mulPose(Vector3f.XP.rotationDegrees(90));
                if ((offsetArr = ThutWearables.config.renderOffsets.get(9)) != null) mat.translate(offsetArr[0],
                        offsetArr[1], offsetArr[2]);
                render = true;
            }
            else
            {
                if (ThutWearables.config.renderBlacklist.contains(10)) break;
                mat.translate(0.25, -0.1, 0.0);
                mat.mulPose(Vector3f.YP.rotationDegrees(90));
                mat.mulPose(Vector3f.XP.rotationDegrees(90));
                if ((offsetArr = ThutWearables.config.renderOffsets.get(10)) != null) mat.translate(offsetArr[0],
                        offsetArr[1], offsetArr[2]);
                Utils.mirror(1, 0, 0, mat);
                render = true;
            }
            break;
        case EYE:
            if (ThutWearables.config.renderBlacklist.contains(11)) break;
            if ((offsetArr = ThutWearables.config.renderOffsets.get(11)) != null) mat.translate(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            render = true;
            break;
        case HAT:
            if (ThutWearables.config.renderBlacklist.contains(12)) break;
            if ((offsetArr = ThutWearables.config.renderOffsets.get(12)) != null) mat.translate(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            render = true;
            break;
        default:
            break;

        }
        return render;
    }

    public static void render(final MatrixStack mat, final IRenderTypeBuffer buff, final IWearable wearable,
            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
            final float partialTicks, final boolean thinArms, final int brightness, final int overlay,
            final IHasHead theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
            return;
        }
        mat.pushPose();
        Head.preOffset(mat, wearer.isBaby(), wearer.isShiftKeyDown());
        // Translate to head
        theModel.getHead().translateAndRotate(mat);
        if (Head.postOffset(mat, index, slot)) wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness,
                overlay);
        mat.popPose();
    }

}
