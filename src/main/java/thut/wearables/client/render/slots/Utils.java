package thut.wearables.client.render.slots;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;

public class Utils
{
    public static void mirror(float x, float y, float z, final PoseStack mat)
    {
        if (x == 0) x = 1;
        else x = -1;
        if (y == 0) y = 1;
        else y = -1;
        if (z == 0) z = 1;
        else z = -1;
        final Matrix3f norms = mat.last().normal().copy();
        mat.scale(x, y, z);
        mat.last().normal().load(norms);

    }
}
