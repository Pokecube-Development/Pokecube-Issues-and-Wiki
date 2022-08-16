package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import thut.api.maths.vecmath.Mat4f;
import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.model.VectorMath;

/** Misc helper methods. */
public class Helpers
{
    static final Vec3f X_AXIS = new Vec3f(1.0F, 0.0F, 0.0F);
    static final Vec3f Y_AXIS = new Vec3f(0.0F, 1.0F, 0.0F);
    static final Vec3f Z_AXIS = new Vec3f(0.0F, 0.0F, 1.0F);

    /**
     * Ensures that the given index will fit in the list.
     *
     * @param list array to ensure has capacity
     * @param i    index to check.
     */
    public static void ensureFits(final ArrayList<?> list, final int index)
    {
        while (list.size() <= index) list.add(null);
    }

    /**
     * Makes a new matrix4f for the given values This works as follows: A blank
     * matrix4f is made via new Matrix4f(), then the matrix is translated by x1,
     * y1, z1, and then it is rotated by zr, yr and xr, in that order, along
     * their respective axes.
     */
    public static Mat4f makeMatrix(final float xl, final float yl, final float zl, final float xr, final float yr,
            final float zr)
    {
        return VectorMath.fromVector6f(xl, yl, zl, xr, yr, zr);
    }
}