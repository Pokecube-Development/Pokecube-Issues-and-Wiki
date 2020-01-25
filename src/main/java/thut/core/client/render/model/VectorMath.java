package thut.core.client.render.model;

import thut.api.maths.vecmath.Matrix4f;
import thut.api.maths.vecmath.Vector3f;

public class VectorMath
{
    static final Vector3f X_AXIS = new Vector3f(1.0F, 0.0F, 0.0F);
    static final Vector3f Y_AXIS = new Vector3f(0.0F, 1.0F, 0.0F);
    static final Vector3f Z_AXIS = new Vector3f(0.0F, 0.0F, 1.0F);

    public static void cleanSmall(final Matrix4f matrix)
    {
        if (Math.abs(matrix.m00) < 1e-6) matrix.m00 = 0;
        if (Math.abs(matrix.m01) < 1e-6) matrix.m01 = 0;
        if (Math.abs(matrix.m02) < 1e-6) matrix.m02 = 0;
        if (Math.abs(matrix.m03) < 1e-6) matrix.m03 = 0;

        if (Math.abs(matrix.m10) < 1e-6) matrix.m10 = 0;
        if (Math.abs(matrix.m11) < 1e-6) matrix.m11 = 0;
        if (Math.abs(matrix.m12) < 1e-6) matrix.m12 = 0;
        if (Math.abs(matrix.m13) < 1e-6) matrix.m13 = 0;

        if (Math.abs(matrix.m20) < 1e-6) matrix.m20 = 0;
        if (Math.abs(matrix.m21) < 1e-6) matrix.m21 = 0;
        if (Math.abs(matrix.m22) < 1e-6) matrix.m22 = 0;
        if (Math.abs(matrix.m23) < 1e-6) matrix.m23 = 0;

        if (Math.abs(matrix.m30) < 1e-6) matrix.m30 = 0;
        if (Math.abs(matrix.m31) < 1e-6) matrix.m31 = 0;
        if (Math.abs(matrix.m32) < 1e-6) matrix.m32 = 0;
        if (Math.abs(matrix.m33) < 1e-6) matrix.m33 = 0;
    }

    public static Matrix4f fromFloat(final float val)
    {
        return VectorMath.fromVector6f(val, val, val, val, val, val);
    }

    public static Matrix4f fromFloatArray(final float[] vals)
    {
        return VectorMath.fromVector6f(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
    }

    public static Matrix4f fromVector6f(final float xl, final float yl, final float zl, final float xr, final float yr,
            final float zr)
    {
        final Vector3f loc = new Vector3f(xl, yl, zl);
        final Matrix4f ret = new Matrix4f();
        VectorMath.translate(loc, ret);
        VectorMath.rotate(zr, VectorMath.Z_AXIS, ret);
        VectorMath.rotate(yr, VectorMath.Y_AXIS, ret);
        VectorMath.rotate(xr, VectorMath.X_AXIS, ret);
        VectorMath.cleanSmall(ret);
        return ret;
    }

    public static Matrix4f fromVector6f(final Vector6f vector)
    {
        return VectorMath.fromVector6f(vector.vector1.x, vector.vector1.y, vector.vector1.z, vector.vector2.x,
                vector.vector2.y, vector.vector2.z);
    }

    public static Matrix4f rotate(final float angle, final Vector3f axis, final Matrix4f matrix)
    {
        return VectorMath.rotate(angle, axis, matrix, matrix);
    }

    /**
     * Rotates the source matrix around the given axis the specified angle and
     * put the result in the destination matrix.
     *
     * @param angle
     *            the angle, in radians.
     * @param axis
     *            The vector representing the rotation axis. Must be normalized.
     * @param src
     *            The matrix to rotate
     * @param dest
     *            The matrix to put the result, or null if a new matrix is to be
     *            created
     * @return The rotated matrix
     */
    public static Matrix4f rotate(final float angle, final Vector3f axis, final Matrix4f src, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();
        final float c = (float) Math.cos(angle);
        final float s = (float) Math.sin(angle);
        final float oneminusc = 1.0f - c;
        final float xy = axis.x * axis.y;
        final float yz = axis.y * axis.z;
        final float xz = axis.x * axis.z;
        final float xs = axis.x * s;
        final float ys = axis.y * s;
        final float zs = axis.z * s;

        final float f00 = axis.x * axis.x * oneminusc + c;
        final float f01 = xy * oneminusc + zs;
        final float f02 = xz * oneminusc - ys;
        // n[3] not used
        final float f10 = xy * oneminusc - zs;
        final float f11 = axis.y * axis.y * oneminusc + c;
        final float f12 = yz * oneminusc + xs;
        // n[7] not used
        final float f20 = xz * oneminusc + ys;
        final float f21 = yz * oneminusc - xs;
        final float f22 = axis.z * axis.z * oneminusc + c;

        final float t00 = src.m00 * f00 + src.m10 * f01 + src.m20 * f02;
        final float t01 = src.m01 * f00 + src.m11 * f01 + src.m21 * f02;
        final float t02 = src.m02 * f00 + src.m12 * f01 + src.m22 * f02;
        final float t03 = src.m03 * f00 + src.m13 * f01 + src.m23 * f02;
        final float t10 = src.m00 * f10 + src.m10 * f11 + src.m20 * f12;
        final float t11 = src.m01 * f10 + src.m11 * f11 + src.m21 * f12;
        final float t12 = src.m02 * f10 + src.m12 * f11 + src.m22 * f12;
        final float t13 = src.m03 * f10 + src.m13 * f11 + src.m23 * f12;
        dest.m20 = src.m00 * f20 + src.m10 * f21 + src.m20 * f22;
        dest.m21 = src.m01 * f20 + src.m11 * f21 + src.m21 * f22;
        dest.m22 = src.m02 * f20 + src.m12 * f21 + src.m22 * f22;
        dest.m23 = src.m03 * f20 + src.m13 * f21 + src.m23 * f22;
        dest.m00 = t00;
        dest.m01 = t01;
        dest.m02 = t02;
        dest.m03 = t03;
        dest.m10 = t10;
        dest.m11 = t11;
        dest.m12 = t12;
        dest.m13 = t13;
        return dest;
    }

    public static Matrix4f translate(final Vector3f vec, final Matrix4f matrix)
    {
        return VectorMath.translate(vec, matrix, matrix);
    }

    public static Matrix4f translate(final Vector3f vec, final Matrix4f src, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();

        dest.m30 += src.m00 * vec.x + src.m10 * vec.y + src.m20 * vec.z;
        dest.m31 += src.m01 * vec.x + src.m11 * vec.y + src.m21 * vec.z;
        dest.m32 += src.m02 * vec.x + src.m12 * vec.y + src.m22 * vec.z;
        dest.m33 += src.m03 * vec.x + src.m13 * vec.y + src.m23 * vec.z;

        return dest;
    }
}
