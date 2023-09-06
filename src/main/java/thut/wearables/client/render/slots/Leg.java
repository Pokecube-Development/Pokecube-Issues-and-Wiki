package thut.wearables.client.render.slots;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
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
    public static void render(final GuiGraphics graphics, final MultiBufferSource buff, final IWearable wearable,
                              final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                              final float partialTicks, final boolean thinArms, final int brightness, final int overlay,
                              final HumanoidModel<?> theModel)
    {
        if (wearable == null) return;

        if (wearable.customOffsets())
        {
            graphics.pose().scale(1, -1, -1);
            graphics.pose().mulPose(Axis.YP.rotationDegrees(180));
            wearable.renderWearable(graphics.pose(), buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
            return;
        }
        float[] offsetArr;

        if (wearer.isCrouching() && (offsetArr = ThutWearables.config.renderOffsetsSneak.get(4 + index)) != null)
            graphics.pose().translate(offsetArr[0], offsetArr[1], offsetArr[2]);

        if (index == 0) theModel.rightLeg.translateAndRotate(graphics.pose());
        else theModel.leftLeg.translateAndRotate(graphics.pose());

        graphics.pose().translate(0.0F, 0.4375F, 0.0625F);

        if ((offsetArr = ThutWearables.config.renderOffsets.get(4 + index)) != null)
            graphics.pose().translate(offsetArr[0], offsetArr[1], offsetArr[2]);

        final boolean render = !ThutWearables.config.renderBlacklist.contains(4 + index);
        // Mirror left leg.
        if (index == 1) Utils.mirror(1, 0, 0, graphics);
        if (render)
        {
            graphics.pose().scale(1, -1, -1);
            graphics.pose().mulPose(Axis.YP.rotationDegrees(180));
            wearable.renderWearable(graphics.pose(), buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

}
