package thut.wearables.client.render.slots;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.lib.AxisAngles;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class Head
{
    public static void preOffset(final GuiGraphics graphics, final boolean childModel, final boolean sneaking)
    {
        float[] offsetArr = new float[3];
        if (sneaking && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(9)) != null)
            graphics.pose().translate(offsetArr[0], offsetArr[1], offsetArr[2]);
        if (childModel)
        {
            final float af = 2.0F;
            final float af1 = 1.4F;
            graphics.pose().translate(0.0F, 0.5F, 0.0F);
            graphics.pose().scale(af1 / af, af1 / af, af1 / af);
            graphics.pose().translate(0.0F, 16.0F, 0.0F);
        }
    }

    public static boolean postOffset(final GuiGraphics graphics, final int index, final EnumWearable slot)
    {
        float[] offsetArr = new float[3];
        graphics.pose().translate(0, -0.25f, 0);
        boolean render = false;
        switch (slot)
        {
        case EAR:
            if (index == 0)
            {
                if (ThutWearables.config.renderBlacklist.contains(9)) break;
                graphics.pose().translate(-0.25, -0.1, 0.0);
                graphics.pose().mulPose(Axis.YP.rotationDegrees(90));
                graphics.pose().mulPose(Axis.XP.rotationDegrees(90));
                if ((offsetArr = ThutWearables.config.renderOffsets.get(9)) != null)
                    graphics.pose().translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                render = true;
            }
            else
            {
                if (ThutWearables.config.renderBlacklist.contains(10)) break;
                graphics.pose().translate(0.25, -0.1, 0.0);
                graphics.pose().mulPose(Axis.YP.rotationDegrees(90));
                graphics.pose().mulPose(Axis.XP.rotationDegrees(90));
                if ((offsetArr = ThutWearables.config.renderOffsets.get(10)) != null)
                    graphics.pose().translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                Utils.mirror(0, 1, 0, graphics);
                render = true;
            }
            break;
        case EYE:
            if (ThutWearables.config.renderBlacklist.contains(11)) break;
            if ((offsetArr = ThutWearables.config.renderOffsets.get(11)) != null)
                graphics.pose().translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            render = true;
            break;
        case HAT:
            if (ThutWearables.config.renderBlacklist.contains(12)) break;
            if ((offsetArr = ThutWearables.config.renderOffsets.get(12)) != null)
                graphics.pose().translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            render = true;
            break;
        default:
            break;

        }
        return render;
    }

    public static void render(final GuiGraphics graphics, final MultiBufferSource buff, final IWearable wearable,
                              final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                              final float partialTicks, final boolean thinArms, final int brightness, final int overlay,
                              final HeadedModel theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            graphics.pose().scale(1, -1, -1);
            graphics.pose().mulPose(Axis.YP.rotationDegrees(180));
            wearable.renderWearable(graphics.pose(), buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
            return;
        }
        Head.preOffset(graphics, wearer.isBaby(), wearer.isShiftKeyDown());
        // Translate to head
        theModel.getHead().translateAndRotate(graphics.pose());
        if (Head.postOffset(graphics, index, slot))
        {
            graphics.pose().scale(1, -1, -1);
            graphics.pose().mulPose(Axis.YP.rotationDegrees(180));
            wearable.renderWearable(graphics.pose(), buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

}
