package thut.wearables.client.render.slots;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class Arm
{
    public static void render(final IWearable wearable, final EnumWearable slot, final int index,
            final LivingEntity wearer, final ItemStack stack, final float partialTicks, final boolean thin,
            final float scale, final IHasArm theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            wearable.renderWearable(slot, index, wearer, stack, partialTicks);
            return;
        }
        float[] offsetArr;
        final boolean sneak = wearer.isSneaking();
        GlStateManager.pushMatrix();
        if (wearer.isSneaking()) GlStateManager.translatef(0.0F, 0.23125F, 0.01F);
        boolean render = false;
        // Right side
        if (index == 0) switch (slot)
        {
        case FINGER:
            if (ThutWearables.config.renderBlacklist.contains(0)) break;
            if (sneak && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(0)) != null) GlStateManager
                    .translatef(offsetArr[0], offsetArr[1], offsetArr[2]);
            theModel.postRenderArm(scale, HandSide.RIGHT);
            GlStateManager.translatef(-0.0625F, 0.59F, 0.0625F);
            if ((offsetArr = ThutWearables.config.renderOffsets.get(0)) != null) GlStateManager.translatef(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            if (thin)
            {
                GlStateManager.translatef(0.025f, 0, 0);
                GlStateManager.scalef(0.75f, 1, 1);
            }
            render = true;
            break;
        case WRIST:
            if (ThutWearables.config.renderBlacklist.contains(2)) break;
            if (sneak && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(2)) != null) GlStateManager
                    .translatef(offsetArr[0], offsetArr[1], offsetArr[2]);
            theModel.postRenderArm(scale, HandSide.RIGHT);
            GlStateManager.translatef(-0.0625F, 0.4375F, 0.0625F);
            if ((offsetArr = ThutWearables.config.renderOffsets.get(2)) != null) GlStateManager.translatef(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            if (thin)
            {
                GlStateManager.translatef(0.025f, 0, 0);
                GlStateManager.scalef(0.75f, 1, 1);
            }
            render = true;
            break;
        default:
            break;
        }
        // Left side
        else switch (slot)
        {
        case FINGER:
            if (ThutWearables.config.renderBlacklist.contains(1)) break;
            if (sneak && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(1)) != null) GlStateManager
                    .translatef(offsetArr[0], offsetArr[1], offsetArr[2]);
            theModel.postRenderArm(scale, HandSide.LEFT);
            GlStateManager.translatef(0.0625F, 0.59F, 0.0625F);
            if ((offsetArr = ThutWearables.config.renderOffsets.get(1)) != null) GlStateManager.translatef(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            if (thin)
            {
                GlStateManager.translatef(-0.025f, 0, 0);
                GlStateManager.scalef(0.75f, 1, 1);
            }
            GlStateManager.scalef(-1, 1, 1); // Left is mirrored
            render = true;
            break;
        case WRIST:
            if (ThutWearables.config.renderBlacklist.contains(3)) break;
            if (sneak && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(3)) != null) GlStateManager
                    .translatef(offsetArr[0], offsetArr[1], offsetArr[2]);
            theModel.postRenderArm(scale, HandSide.LEFT);
            GlStateManager.translatef(0.0625F, 0.4375F, 0.0625F);
            if ((offsetArr = ThutWearables.config.renderOffsets.get(3)) != null) GlStateManager.translatef(offsetArr[0],
                    offsetArr[1], offsetArr[2]);
            if (thin)
            {
                GlStateManager.translatef(-0.025f, 0, 0);
                GlStateManager.scalef(0.75f, 1, 1);
            }
            GlStateManager.scalef(-1, 1, 1); // Left is mirrored
            render = true;
            break;
        default:
            break;
        }
        if (render) wearable.renderWearable(slot, index, wearer, stack, partialTicks);
        GlStateManager.popMatrix();
    }
}
