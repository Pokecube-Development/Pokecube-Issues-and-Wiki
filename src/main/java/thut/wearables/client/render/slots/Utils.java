package thut.wearables.client.render.slots;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Matrix3f;

public class Utils
{
    public static void mirror(float x, float y, float z, MatrixStack mat)
    {
        if (x == 0) x = 1;
        else x = -1;
        if (y == 0) y = 1;
        else y = -1;
        if (z == 0) z = 1;
        else z = -1;
        Matrix3f norms = mat.getLast().getNormalMatrix().copy();
        mat.scale(x, y, z);
        mat.getLast().getNormalMatrix().setFrom(norms);

    }
}
