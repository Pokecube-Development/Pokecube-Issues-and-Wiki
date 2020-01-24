package thut.wearables.client.render.slots;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class Head
{
    public static void render(final IWearable wearable, final EnumWearable slot, final int index,
            final LivingEntity wearer, final ItemStack stack, final float partialTicks, final boolean thinArms,
            final float scale, final IHasHead theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            wearable.renderWearable(slot, index, wearer, stack, partialTicks);
            return;
        }
        float[] offsetArr;

        GlStateManager.pushMatrix();
        if (wearer.isSneaking())
        {
            GlStateManager.translatef(0.0F, 0.2F, 0.0F);
            if ((offsetArr = ThutWearables.config.renderOffsetsSneak.get(9)) != null) GlStateManager.translatef(
                    offsetArr[0], offsetArr[1], offsetArr[2]);
        }
        if (wearer.isChild())
        {
            final float af = 2.0F;
            final float af1 = 1.4F;
            GlStateManager.translatef(0.0F, 0.5F * scale, 0.0F);
            GlStateManager.scalef(af1 / af, af1 / af, af1 / af);
            GlStateManager.translatef(0.0F, 16.0F * scale, 0.0F);
        }

        // Translate to head
        theModel.func_217142_c(0.0625F);

        GlStateManager.translatef(0, -0.25f, 0);
        GlStateManager.pushMatrix();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        boolean render = false;
        switch (slot)
        {
        case EAR:
            if (index == 0)
            {
                if (ThutWearables.config.renderBlacklist.contains(9)) break;
                GL11.glTranslated(-0.25, -0.1, 0.0);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                if ((offsetArr = ThutWearables.config.renderOffsets.get(9)) != null) GlStateManager.translatef(
                        offsetArr[0], offsetArr[1], offsetArr[2]);
                render = true;
            }
            else
            {
                if (ThutWearables.config.renderBlacklist.contains(10)) break;
                GL11.glTranslated(0.25, -0.1, 0.0);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                if ((offsetArr = ThutWearables.config.renderOffsets.get(10)) != null) GlStateManager.translatef(
                        offsetArr[0], offsetArr[1], offsetArr[2]);
                GlStateManager.scalef(-1, 1, 1); // This mirrors it.
                render = true;
            }
            break;
        case EYE:
            if (ThutWearables.config.renderBlacklist.contains(11)) break;
            if ((offsetArr = ThutWearables.config.renderOffsets.get(11)) != null) GlStateManager.translatef(
                    offsetArr[0], offsetArr[1], offsetArr[2]);
            render = true;
            break;
        case HAT:
            if (ThutWearables.config.renderBlacklist.contains(12)) break;
            if ((offsetArr = ThutWearables.config.renderOffsets.get(12)) != null) GlStateManager.translatef(
                    offsetArr[0], offsetArr[1], offsetArr[2]);
            render = true;
            break;
        default:
            break;

        }
        if (render) wearable.renderWearable(slot, index, wearer, stack, partialTicks);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

}
