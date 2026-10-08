package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import org.joml.Matrix4f;
import pokecube.mobs.client.smd.VectorMath;

/** Misc helper methods. */
public class Helpers
{
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
    public static Matrix4f makeMatrix(final float xl, final float yl, final float zl, final float xr, final float yr,
            final float zr)
    {
        return VectorMath.fromVector6f(xl, yl, zl, xr, yr, zr);
    }
}