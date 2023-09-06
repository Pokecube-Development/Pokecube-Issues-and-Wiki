package thut.wearables.client.render.slots;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3f;

public class Utils
{
    public static void mirror(float x, float y, float z, final PoseStack poseStack)
    {
        if (x == 0) x = 1;
        else x = -1;
        if (y == 0) y = 1;
        else y = -1;
        if (z == 0) z = 1;
        else z = -1;
        // TODO: Check this
        final Matrix3f norms = poseStack.last().normal()/*.copy()*/;
        poseStack.scale(x, y, z);
        poseStack.last().normal().add(norms);

    }
}
