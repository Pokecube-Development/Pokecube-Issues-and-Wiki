package thut.wearables.client.render.slots;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class Arm
{
    public static void render(final MatrixStack mat, final IRenderTypeBuffer buff, final IWearable wearable,
            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
            final float partialTicks, final boolean thin, final int brightness, final int overlay,
            final IHasArm theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
            return;
        }
        float[] offsetArr;
        final boolean sneak = wearer.isCrouching();
        mat.push();

        boolean render = false;
        // Right side
        if (index == 0) switch (slot)
        {
        case FINGER:
            if (ThutWearables.config.renderBlacklist.contains(0)) break;
            if (sneak && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(0)) != null)
                mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            theModel.translateHand(HandSide.RIGHT, mat);
            mat.translate(-0.0625F, 0.59F, 0.0625F);
            if ((offsetArr = ThutWearables.config.renderOffsets.get(0)) != null)
                mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            if (thin)
            {
                mat.translate(0.025f, 0, 0);
                mat.scale(0.75f, 1, 1);
            }
            render = true;
            break;
        case WRIST:
            if (ThutWearables.config.renderBlacklist.contains(2)) break;
            if (sneak && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(2)) != null)
                mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            theModel.translateHand(HandSide.RIGHT, mat);
            mat.translate(-0.0625F, 0.4375F, 0.0625F);
            if ((offsetArr = ThutWearables.config.renderOffsets.get(2)) != null)
                mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            if (thin)
            {
                mat.translate(0.025f, 0, 0);
                mat.scale(0.75f, 1, 1);
            }
            render = true;
            break;
        default:
            break;
        }
        // Left side
        else
        {
            switch (slot)
            {
            case FINGER:
                if (ThutWearables.config.renderBlacklist.contains(1)) break;
                if (sneak && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(1)) != null)
                    mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                theModel.translateHand(HandSide.LEFT, mat);
                mat.translate(0.0625F, 0.59F, 0.0625F);
                if ((offsetArr = ThutWearables.config.renderOffsets.get(1)) != null)
                    mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                if (thin)
                {
                    mat.translate(-0.025f, 0, 0);
                    mat.scale(0.75f, 1, 1);
                }
                render = true;
                break;
            case WRIST:
                if (ThutWearables.config.renderBlacklist.contains(3)) break;
                if (sneak && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(3)) != null)
                    mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                theModel.translateHand(HandSide.LEFT, mat);
                mat.translate(0.0625F, 0.4375F, 0.0625F);
                if ((offsetArr = ThutWearables.config.renderOffsets.get(3)) != null)
                    mat.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                if (thin)
                {
                    mat.translate(-0.025f, 0, 0);
                    mat.scale(0.75f, 1, 1);
                }
                render = true;
                break;
            default:
                break;
            }
            Utils.mirror(1, 0, 0, mat);
        }
        if (render) wearable.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        mat.pop();
    }
}
