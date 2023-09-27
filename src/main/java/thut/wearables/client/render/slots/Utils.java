package thut.wearables.client.render.slots;

import org.joml.Matrix3f;

import com.mojang.blaze3d.vertex.PoseStack;

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
        Matrix3f norms = new Matrix3f(poseStack.last().normal());
        poseStack.scale(x, y, z);
        poseStack.last().normal().set(norms);

    }
}
