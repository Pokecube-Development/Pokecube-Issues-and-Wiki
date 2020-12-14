package thut.wearables.client.render.slots;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.math.vector.Matrix3f;

public class Utils
{
    public static void mirror(float x, float y, float z, final MatrixStack mat)
    {
        if (x == 0) x = 1;
        else x = -1;
        if (y == 0) y = 1;
        else y = -1;
        if (z == 0) z = 1;
        else z = -1;
        final Matrix3f norms = mat.getLast().getNormal().copy();
        mat.scale(x, y, z);
        mat.getLast().getNormal().set(norms);

    }
}
