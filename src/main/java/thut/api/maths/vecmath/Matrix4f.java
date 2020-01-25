/*
 * Copyright (c) 2002-2008 LWJGL Project
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of 'LWJGL' nor the names of
 * its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package thut.api.maths.vecmath;

import java.io.Serializable;
import java.nio.FloatBuffer;

/**
 * Holds a 4x4 float matrix.
 *
 * @author foo
 */
public class Matrix4f extends Matrix implements Serializable
{
    private static final long serialVersionUID = 1L;

    public float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33;

    /**
     * Construct a new matrix, initialized to the identity.
     */
    public Matrix4f()
    {
        super();
        this.setIdentity();
    }

    public Matrix4f(final Matrix4f src)
    {
        super();
        this.load(src);
    }

    /**
     * Returns a string representation of this matrix
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append(this.m00).append(' ').append(this.m10).append(' ').append(this.m20).append(' ').append(this.m30)
                .append('\n');
        buf.append(this.m01).append(' ').append(this.m11).append(' ').append(this.m21).append(' ').append(this.m31)
                .append('\n');
        buf.append(this.m02).append(' ').append(this.m12).append(' ').append(this.m22).append(' ').append(this.m32)
                .append('\n');
        buf.append(this.m03).append(' ').append(this.m13).append(' ').append(this.m23).append(' ').append(this.m33)
                .append('\n');
        return buf.toString();
    }

    /**
     * Set this matrix to be the identity matrix.
     *
     * @return this
     */
    @Override
    public Matrix setIdentity()
    {
        return Matrix4f.setIdentity(this);
    }

    /**
     * Set the given matrix to be the identity matrix.
     *
     * @param m
     *            The matrix to set to the identity
     * @return m
     */
    public static Matrix4f setIdentity(final Matrix4f m)
    {
        m.m00 = 1.0f;
        m.m01 = 0.0f;
        m.m02 = 0.0f;
        m.m03 = 0.0f;
        m.m10 = 0.0f;
        m.m11 = 1.0f;
        m.m12 = 0.0f;
        m.m13 = 0.0f;
        m.m20 = 0.0f;
        m.m21 = 0.0f;
        m.m22 = 1.0f;
        m.m23 = 0.0f;
        m.m30 = 0.0f;
        m.m31 = 0.0f;
        m.m32 = 0.0f;
        m.m33 = 1.0f;

        return m;
    }

    /**
     * Performs an SVD normalization of this matrix in order to acquire
     * the normalized rotational component; the values are placed into
     * the Matrix3d parameter.
     *
     * @param m1
     *            matrix into which the rotational component is placed
     */
    public final void get(final Matrix3d m1)
    {

        final double[] tmp_rot = new double[9];  // scratch matrix
        final double[] tmp_scale = new double[3];  // scratch matrix

        this.getScaleRotate(tmp_scale, tmp_rot);

        m1.m00 = tmp_rot[0];
        m1.m01 = tmp_rot[1];
        m1.m02 = tmp_rot[2];

        m1.m10 = tmp_rot[3];
        m1.m11 = tmp_rot[4];
        m1.m12 = tmp_rot[5];

        m1.m20 = tmp_rot[6];
        m1.m21 = tmp_rot[7];
        m1.m22 = tmp_rot[8];

    }

    /**
     * Performs an SVD normalization of this matrix in order to acquire
     * the normalized rotational component; the values are placed into
     * the Matrix3f parameter.
     *
     * @param m1
     *            matrix into which the rotational component is placed
     */
    public final void get(final Matrix3f m1)
    {
        final double[] tmp_rot = new double[9];  // scratch matrix
        final double[] tmp_scale = new double[3];  // scratch matrix

        this.getScaleRotate(tmp_scale, tmp_rot);

        m1.m00 = (float) tmp_rot[0];
        m1.m01 = (float) tmp_rot[1];
        m1.m02 = (float) tmp_rot[2];

        m1.m10 = (float) tmp_rot[3];
        m1.m11 = (float) tmp_rot[4];
        m1.m12 = (float) tmp_rot[5];

        m1.m20 = (float) tmp_rot[6];
        m1.m21 = (float) tmp_rot[7];
        m1.m22 = (float) tmp_rot[8];

    }

    private final void getScaleRotate(final double scales[], final double rots[])
    {

        final double[] tmp = new double[9];  // scratch matrix
        tmp[0] = this.m00;
        tmp[1] = this.m01;
        tmp[2] = this.m02;

        tmp[3] = this.m10;
        tmp[4] = this.m11;
        tmp[5] = this.m12;

        tmp[6] = this.m20;
        tmp[7] = this.m21;
        tmp[8] = this.m22;

        Matrix3d.compute_svd(tmp, scales, rots);

        return;
    }

    /**
     * Set this matrix to 0.
     *
     * @return this
     */
    @Override
    public Matrix setZero()
    {
        return Matrix4f.setZero(this);
    }

    /**
     * Set the given matrix to 0.
     *
     * @param m
     *            The matrix to set to 0
     * @return m
     */
    public static Matrix4f setZero(final Matrix4f m)
    {
        m.m00 = 0.0f;
        m.m01 = 0.0f;
        m.m02 = 0.0f;
        m.m03 = 0.0f;
        m.m10 = 0.0f;
        m.m11 = 0.0f;
        m.m12 = 0.0f;
        m.m13 = 0.0f;
        m.m20 = 0.0f;
        m.m21 = 0.0f;
        m.m22 = 0.0f;
        m.m23 = 0.0f;
        m.m30 = 0.0f;
        m.m31 = 0.0f;
        m.m32 = 0.0f;
        m.m33 = 0.0f;

        return m;
    }

    /**
     * Load from another matrix4f
     *
     * @param src
     *            The source matrix
     * @return this
     */
    public Matrix4f load(final Matrix4f src)
    {
        return Matrix4f.load(src, this);
    }

    /**
     * Copy the source matrix to the destination matrix
     *
     * @param src
     *            The source matrix
     * @param dest
     *            The destination matrix, or null of a new one is to be created
     * @return The copied matrix
     */
    public static Matrix4f load(final Matrix4f src, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();
        dest.m00 = src.m00;
        dest.m01 = src.m01;
        dest.m02 = src.m02;
        dest.m03 = src.m03;
        dest.m10 = src.m10;
        dest.m11 = src.m11;
        dest.m12 = src.m12;
        dest.m13 = src.m13;
        dest.m20 = src.m20;
        dest.m21 = src.m21;
        dest.m22 = src.m22;
        dest.m23 = src.m23;
        dest.m30 = src.m30;
        dest.m31 = src.m31;
        dest.m32 = src.m32;
        dest.m33 = src.m33;

        return dest;
    }

    /**
     * Load from a float buffer. The buffer stores the matrix in column major
     * (OpenGL) order.
     *
     * @param buf
     *            A float buffer to read from
     * @return this
     */
    @Override
    public Matrix load(final FloatBuffer buf)
    {

        this.m00 = buf.get();
        this.m01 = buf.get();
        this.m02 = buf.get();
        this.m03 = buf.get();
        this.m10 = buf.get();
        this.m11 = buf.get();
        this.m12 = buf.get();
        this.m13 = buf.get();
        this.m20 = buf.get();
        this.m21 = buf.get();
        this.m22 = buf.get();
        this.m23 = buf.get();
        this.m30 = buf.get();
        this.m31 = buf.get();
        this.m32 = buf.get();
        this.m33 = buf.get();

        return this;
    }

    /**
     * Load from a float buffer. The buffer stores the matrix in row major
     * (maths) order.
     *
     * @param buf
     *            A float buffer to read from
     * @return this
     */
    @Override
    public Matrix loadTranspose(final FloatBuffer buf)
    {

        this.m00 = buf.get();
        this.m10 = buf.get();
        this.m20 = buf.get();
        this.m30 = buf.get();
        this.m01 = buf.get();
        this.m11 = buf.get();
        this.m21 = buf.get();
        this.m31 = buf.get();
        this.m02 = buf.get();
        this.m12 = buf.get();
        this.m22 = buf.get();
        this.m32 = buf.get();
        this.m03 = buf.get();
        this.m13 = buf.get();
        this.m23 = buf.get();
        this.m33 = buf.get();

        return this;
    }

    /**
     * Store this matrix in a float buffer. The matrix is stored in column
     * major (openGL) order.
     *
     * @param buf
     *            The buffer to store this matrix in
     */
    @Override
    public Matrix store(final FloatBuffer buf)
    {
        buf.put(this.m00);
        buf.put(this.m01);
        buf.put(this.m02);
        buf.put(this.m03);
        buf.put(this.m10);
        buf.put(this.m11);
        buf.put(this.m12);
        buf.put(this.m13);
        buf.put(this.m20);
        buf.put(this.m21);
        buf.put(this.m22);
        buf.put(this.m23);
        buf.put(this.m30);
        buf.put(this.m31);
        buf.put(this.m32);
        buf.put(this.m33);
        return this;
    }

    /**
     * Store this matrix in a float buffer. The matrix is stored in row
     * major (maths) order.
     *
     * @param buf
     *            The buffer to store this matrix in
     */
    @Override
    public Matrix storeTranspose(final FloatBuffer buf)
    {
        buf.put(this.m00);
        buf.put(this.m10);
        buf.put(this.m20);
        buf.put(this.m30);
        buf.put(this.m01);
        buf.put(this.m11);
        buf.put(this.m21);
        buf.put(this.m31);
        buf.put(this.m02);
        buf.put(this.m12);
        buf.put(this.m22);
        buf.put(this.m32);
        buf.put(this.m03);
        buf.put(this.m13);
        buf.put(this.m23);
        buf.put(this.m33);
        return this;
    }

    /**
     * Store the rotation portion of this matrix in a float buffer. The matrix
     * is stored in column
     * major (openGL) order.
     *
     * @param buf
     *            The buffer to store this matrix in
     */
    public Matrix store3f(final FloatBuffer buf)
    {
        buf.put(this.m00);
        buf.put(this.m01);
        buf.put(this.m02);
        buf.put(this.m10);
        buf.put(this.m11);
        buf.put(this.m12);
        buf.put(this.m20);
        buf.put(this.m21);
        buf.put(this.m22);
        return this;
    }

    /**
     * Add two matrices together and place the result in a third matrix.
     *
     * @param left
     *            The left source matrix
     * @param right
     *            The right source matrix
     * @param dest
     *            The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f add(final Matrix4f left, final Matrix4f right, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();

        dest.m00 = left.m00 + right.m00;
        dest.m01 = left.m01 + right.m01;
        dest.m02 = left.m02 + right.m02;
        dest.m03 = left.m03 + right.m03;
        dest.m10 = left.m10 + right.m10;
        dest.m11 = left.m11 + right.m11;
        dest.m12 = left.m12 + right.m12;
        dest.m13 = left.m13 + right.m13;
        dest.m20 = left.m20 + right.m20;
        dest.m21 = left.m21 + right.m21;
        dest.m22 = left.m22 + right.m22;
        dest.m23 = left.m23 + right.m23;
        dest.m30 = left.m30 + right.m30;
        dest.m31 = left.m31 + right.m31;
        dest.m32 = left.m32 + right.m32;
        dest.m33 = left.m33 + right.m33;

        return dest;
    }

    /**
     * Subtract the right matrix from the left and place the result in a third
     * matrix.
     *
     * @param left
     *            The left source matrix
     * @param right
     *            The right source matrix
     * @param dest
     *            The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f sub(final Matrix4f left, final Matrix4f right, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();

        dest.m00 = left.m00 - right.m00;
        dest.m01 = left.m01 - right.m01;
        dest.m02 = left.m02 - right.m02;
        dest.m03 = left.m03 - right.m03;
        dest.m10 = left.m10 - right.m10;
        dest.m11 = left.m11 - right.m11;
        dest.m12 = left.m12 - right.m12;
        dest.m13 = left.m13 - right.m13;
        dest.m20 = left.m20 - right.m20;
        dest.m21 = left.m21 - right.m21;
        dest.m22 = left.m22 - right.m22;
        dest.m23 = left.m23 - right.m23;
        dest.m30 = left.m30 - right.m30;
        dest.m31 = left.m31 - right.m31;
        dest.m32 = left.m32 - right.m32;
        dest.m33 = left.m33 - right.m33;

        return dest;
    }

    /**
     * Multiply the right matrix by the left and place the result in a third
     * matrix.
     *
     * @param left
     *            The left source matrix
     * @param right
     *            The right source matrix
     * @param dest
     *            The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f mul(final Matrix4f left, final Matrix4f right, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();

        final float m00 = left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02 + left.m30 * right.m03;
        final float m01 = left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02 + left.m31 * right.m03;
        final float m02 = left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02 + left.m32 * right.m03;
        final float m03 = left.m03 * right.m00 + left.m13 * right.m01 + left.m23 * right.m02 + left.m33 * right.m03;
        final float m10 = left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12 + left.m30 * right.m13;
        final float m11 = left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12 + left.m31 * right.m13;
        final float m12 = left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12 + left.m32 * right.m13;
        final float m13 = left.m03 * right.m10 + left.m13 * right.m11 + left.m23 * right.m12 + left.m33 * right.m13;
        final float m20 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22 + left.m30 * right.m23;
        final float m21 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22 + left.m31 * right.m23;
        final float m22 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22 + left.m32 * right.m23;
        final float m23 = left.m03 * right.m20 + left.m13 * right.m21 + left.m23 * right.m22 + left.m33 * right.m23;
        final float m30 = left.m00 * right.m30 + left.m10 * right.m31 + left.m20 * right.m32 + left.m30 * right.m33;
        final float m31 = left.m01 * right.m30 + left.m11 * right.m31 + left.m21 * right.m32 + left.m31 * right.m33;
        final float m32 = left.m02 * right.m30 + left.m12 * right.m31 + left.m22 * right.m32 + left.m32 * right.m33;
        final float m33 = left.m03 * right.m30 + left.m13 * right.m31 + left.m23 * right.m32 + left.m33 * right.m33;

        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        dest.m03 = m03;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        dest.m13 = m13;
        dest.m20 = m20;
        dest.m21 = m21;
        dest.m22 = m22;
        dest.m23 = m23;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.m33 = m33;

        return dest;
    }

    /**
     * Transform a Vector by a matrix and return the result in a destination
     * vector.
     *
     * @param left
     *            The left matrix
     * @param right
     *            The right vector
     * @param dest
     *            The destination vector, or null if a new one is to be created
     * @return the destination vector
     */
    public static Vector4f transform(final Matrix4f left, final Vector4f right, Vector4f dest)
    {
        if (dest == null) dest = new Vector4f();

        final float x = left.m00 * right.x + left.m10 * right.y + left.m20 * right.z + left.m30 * right.w;
        final float y = left.m01 * right.x + left.m11 * right.y + left.m21 * right.z + left.m31 * right.w;
        final float z = left.m02 * right.x + left.m12 * right.y + left.m22 * right.z + left.m32 * right.w;
        final float w = left.m03 * right.x + left.m13 * right.y + left.m23 * right.z + left.m33 * right.w;

        dest.x = x;
        dest.y = y;
        dest.z = z;
        dest.w = w;

        return dest;
    }

    /**
     * Transpose this matrix
     *
     * @return this
     */
    @Override
    public Matrix transpose()
    {
        return this.transpose(this);
    }

    /**
     * Translate this matrix
     *
     * @param vec
     *            The vector to translate by
     * @return this
     */
    public Matrix4f translate(final Vector2f vec)
    {
        return this.translate(vec, this);
    }

    /**
     * Translate this matrix
     *
     * @param vec
     *            The vector to translate by
     * @return this
     */
    public Matrix4f translate(final Vector3f vec)
    {
        return this.translate(vec, this);
    }

    /**
     * Scales this matrix
     *
     * @param vec
     *            The vector to scale by
     * @return this
     */
    public Matrix4f scale(final Vector3f vec)
    {
        return Matrix4f.scale(vec, this, this);
    }

    /**
     * Scales the source matrix and put the result in the destination matrix
     *
     * @param vec
     *            The vector to scale by
     * @param src
     *            The source matrix
     * @param dest
     *            The destination matrix, or null if a new matrix is to be
     *            created
     * @return The scaled matrix
     */
    public static Matrix4f scale(final Vector3f vec, final Matrix4f src, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();
        dest.m00 = src.m00 * vec.x;
        dest.m01 = src.m01 * vec.x;
        dest.m02 = src.m02 * vec.x;
        dest.m03 = src.m03 * vec.x;
        dest.m10 = src.m10 * vec.y;
        dest.m11 = src.m11 * vec.y;
        dest.m12 = src.m12 * vec.y;
        dest.m13 = src.m13 * vec.y;
        dest.m20 = src.m20 * vec.z;
        dest.m21 = src.m21 * vec.z;
        dest.m22 = src.m22 * vec.z;
        dest.m23 = src.m23 * vec.z;
        return dest;
    }

    /**
     * Rotates the matrix around the given axis the specified angle
     *
     * @param angle
     *            the angle, in radians.
     * @param axis
     *            The vector representing the rotation axis. Must be normalized.
     * @return this
     */
    public Matrix4f rotate(final float angle, final Vector3f axis)
    {
        return this.rotate(angle, axis, this);
    }

    /**
     * Rotates the matrix around the given axis the specified angle
     *
     * @param angle
     *            the angle, in radians.
     * @param axis
     *            The vector representing the rotation axis. Must be normalized.
     * @param dest
     *            The matrix to put the result, or null if a new matrix is to be
     *            created
     * @return The rotated matrix
     */
    public Matrix4f rotate(final float angle, final Vector3f axis, final Matrix4f dest)
    {
        return Matrix4f.rotate(angle, axis, this, dest);
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

    /**
     * Translate this matrix and stash the result in another matrix
     *
     * @param vec
     *            The vector to translate by
     * @param dest
     *            The destination matrix or null if a new matrix is to be
     *            created
     * @return the translated matrix
     */
    public Matrix4f translate(final Vector3f vec, final Matrix4f dest)
    {
        return Matrix4f.translate(vec, this, dest);
    }

    /**
     * Translate the source matrix and stash the result in the destination
     * matrix
     *
     * @param vec
     *            The vector to translate by
     * @param src
     *            The source matrix
     * @param dest
     *            The destination matrix or null if a new matrix is to be
     *            created
     * @return The translated matrix
     */
    public static Matrix4f translate(final Vector3f vec, final Matrix4f src, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();

        dest.m30 += src.m00 * vec.x + src.m10 * vec.y + src.m20 * vec.z;
        dest.m31 += src.m01 * vec.x + src.m11 * vec.y + src.m21 * vec.z;
        dest.m32 += src.m02 * vec.x + src.m12 * vec.y + src.m22 * vec.z;
        dest.m33 += src.m03 * vec.x + src.m13 * vec.y + src.m23 * vec.z;

        return dest;
    }

    /**
     * Translate this matrix and stash the result in another matrix
     *
     * @param vec
     *            The vector to translate by
     * @param dest
     *            The destination matrix or null if a new matrix is to be
     *            created
     * @return the translated matrix
     */
    public Matrix4f translate(final Vector2f vec, final Matrix4f dest)
    {
        return Matrix4f.translate(vec, this, dest);
    }

    /**
     * Translate the source matrix and stash the result in the destination
     * matrix
     *
     * @param vec
     *            The vector to translate by
     * @param src
     *            The source matrix
     * @param dest
     *            The destination matrix or null if a new matrix is to be
     *            created
     * @return The translated matrix
     */
    public static Matrix4f translate(final Vector2f vec, final Matrix4f src, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();

        dest.m30 += src.m00 * vec.x + src.m10 * vec.y;
        dest.m31 += src.m01 * vec.x + src.m11 * vec.y;
        dest.m32 += src.m02 * vec.x + src.m12 * vec.y;
        dest.m33 += src.m03 * vec.x + src.m13 * vec.y;

        return dest;
    }

    /**
     * Transpose this matrix and place the result in another matrix
     *
     * @param dest
     *            The destination matrix or null if a new matrix is to be
     *            created
     * @return the transposed matrix
     */
    public Matrix4f transpose(final Matrix4f dest)
    {
        return Matrix4f.transpose(this, dest);
    }

    /**
     * Transpose the source matrix and place the result in the destination
     * matrix
     *
     * @param src
     *            The source matrix
     * @param dest
     *            The destination matrix or null if a new matrix is to be
     *            created
     * @return the transposed matrix
     */
    public static Matrix4f transpose(final Matrix4f src, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();
        final float m00 = src.m00;
        final float m01 = src.m10;
        final float m02 = src.m20;
        final float m03 = src.m30;
        final float m10 = src.m01;
        final float m11 = src.m11;
        final float m12 = src.m21;
        final float m13 = src.m31;
        final float m20 = src.m02;
        final float m21 = src.m12;
        final float m22 = src.m22;
        final float m23 = src.m32;
        final float m30 = src.m03;
        final float m31 = src.m13;
        final float m32 = src.m23;
        final float m33 = src.m33;

        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        dest.m03 = m03;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        dest.m13 = m13;
        dest.m20 = m20;
        dest.m21 = m21;
        dest.m22 = m22;
        dest.m23 = m23;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.m33 = m33;

        return dest;
    }

    /**
     * @return the determinant of the matrix
     */
    @Override
    public float determinant()
    {
        float f = this.m00 * (this.m11 * this.m22 * this.m33 + this.m12 * this.m23 * this.m31 + this.m13 * this.m21
                * this.m32 - this.m13 * this.m22 * this.m31 - this.m11 * this.m23 * this.m32 - this.m12 * this.m21
                        * this.m33);
        f -= this.m01 * (this.m10 * this.m22 * this.m33 + this.m12 * this.m23 * this.m30 + this.m13 * this.m20
                * this.m32 - this.m13 * this.m22 * this.m30 - this.m10 * this.m23 * this.m32 - this.m12 * this.m20
                        * this.m33);
        f += this.m02 * (this.m10 * this.m21 * this.m33 + this.m11 * this.m23 * this.m30 + this.m13 * this.m20
                * this.m31 - this.m13 * this.m21 * this.m30 - this.m10 * this.m23 * this.m31 - this.m11 * this.m20
                        * this.m33);
        f -= this.m03 * (this.m10 * this.m21 * this.m32 + this.m11 * this.m22 * this.m30 + this.m12 * this.m20
                * this.m31 - this.m12 * this.m21 * this.m30 - this.m10 * this.m22 * this.m31 - this.m11 * this.m20
                        * this.m32);
        return f;
    }

    /**
     * Calculate the determinant of a 3x3 matrix
     *
     * @return result
     */

    private static float determinant3x3(final float t00, final float t01, final float t02, final float t10,
            final float t11, final float t12, final float t20, final float t21, final float t22)
    {
        return t00 * (t11 * t22 - t12 * t21) + t01 * (t12 * t20 - t10 * t22) + t02 * (t10 * t21 - t11 * t20);
    }

    /**
     * Invert this matrix
     *
     * @return this if successful, null otherwise
     */
    @Override
    public Matrix invert()
    {
        return Matrix4f.invert(this, this);
    }

    /**
     * Invert the source matrix and put the result in the destination
     *
     * @param src
     *            The source matrix
     * @param dest
     *            The destination matrix, or null if a new matrix is to be
     *            created
     * @return The inverted matrix if successful, null otherwise
     */
    public static Matrix4f invert(final Matrix4f src, Matrix4f dest)
    {
        final float determinant = src.determinant();

        if (determinant != 0)
        {
            /*
             * m00 m01 m02 m03
             * m10 m11 m12 m13
             * m20 m21 m22 m23
             * m30 m31 m32 m33
             */
            if (dest == null) dest = new Matrix4f();
            final float determinant_inv = 1f / determinant;

            // first row
            final float t00 = Matrix4f.determinant3x3(src.m11, src.m12, src.m13, src.m21, src.m22, src.m23, src.m31,
                    src.m32, src.m33);
            final float t01 = -Matrix4f.determinant3x3(src.m10, src.m12, src.m13, src.m20, src.m22, src.m23, src.m30,
                    src.m32, src.m33);
            final float t02 = Matrix4f.determinant3x3(src.m10, src.m11, src.m13, src.m20, src.m21, src.m23, src.m30,
                    src.m31, src.m33);
            final float t03 = -Matrix4f.determinant3x3(src.m10, src.m11, src.m12, src.m20, src.m21, src.m22, src.m30,
                    src.m31, src.m32);
            // second row
            final float t10 = -Matrix4f.determinant3x3(src.m01, src.m02, src.m03, src.m21, src.m22, src.m23, src.m31,
                    src.m32, src.m33);
            final float t11 = Matrix4f.determinant3x3(src.m00, src.m02, src.m03, src.m20, src.m22, src.m23, src.m30,
                    src.m32, src.m33);
            final float t12 = -Matrix4f.determinant3x3(src.m00, src.m01, src.m03, src.m20, src.m21, src.m23, src.m30,
                    src.m31, src.m33);
            final float t13 = Matrix4f.determinant3x3(src.m00, src.m01, src.m02, src.m20, src.m21, src.m22, src.m30,
                    src.m31, src.m32);
            // third row
            final float t20 = Matrix4f.determinant3x3(src.m01, src.m02, src.m03, src.m11, src.m12, src.m13, src.m31,
                    src.m32, src.m33);
            final float t21 = -Matrix4f.determinant3x3(src.m00, src.m02, src.m03, src.m10, src.m12, src.m13, src.m30,
                    src.m32, src.m33);
            final float t22 = Matrix4f.determinant3x3(src.m00, src.m01, src.m03, src.m10, src.m11, src.m13, src.m30,
                    src.m31, src.m33);
            final float t23 = -Matrix4f.determinant3x3(src.m00, src.m01, src.m02, src.m10, src.m11, src.m12, src.m30,
                    src.m31, src.m32);
            // fourth row
            final float t30 = -Matrix4f.determinant3x3(src.m01, src.m02, src.m03, src.m11, src.m12, src.m13, src.m21,
                    src.m22, src.m23);
            final float t31 = Matrix4f.determinant3x3(src.m00, src.m02, src.m03, src.m10, src.m12, src.m13, src.m20,
                    src.m22, src.m23);
            final float t32 = -Matrix4f.determinant3x3(src.m00, src.m01, src.m03, src.m10, src.m11, src.m13, src.m20,
                    src.m21, src.m23);
            final float t33 = Matrix4f.determinant3x3(src.m00, src.m01, src.m02, src.m10, src.m11, src.m12, src.m20,
                    src.m21, src.m22);

            // transpose and divide by the determinant
            dest.m00 = t00 * determinant_inv;
            dest.m11 = t11 * determinant_inv;
            dest.m22 = t22 * determinant_inv;
            dest.m33 = t33 * determinant_inv;
            dest.m01 = t10 * determinant_inv;
            dest.m10 = t01 * determinant_inv;
            dest.m20 = t02 * determinant_inv;
            dest.m02 = t20 * determinant_inv;
            dest.m12 = t21 * determinant_inv;
            dest.m21 = t12 * determinant_inv;
            dest.m03 = t30 * determinant_inv;
            dest.m30 = t03 * determinant_inv;
            dest.m13 = t31 * determinant_inv;
            dest.m31 = t13 * determinant_inv;
            dest.m32 = t23 * determinant_inv;
            dest.m23 = t32 * determinant_inv;
            return dest;
        }
        else return null;
    }

    /**
     * Negate this matrix
     *
     * @return this
     */
    @Override
    public Matrix negate()
    {
        return this.negate(this);
    }

    /**
     * Negate this matrix and place the result in a destination matrix.
     *
     * @param dest
     *            The destination matrix, or null if a new matrix is to be
     *            created
     * @return the negated matrix
     */
    public Matrix4f negate(final Matrix4f dest)
    {
        return Matrix4f.negate(this, dest);
    }

    /**
     * Negate this matrix and place the result in a destination matrix.
     *
     * @param src
     *            The source matrix
     * @param dest
     *            The destination matrix, or null if a new matrix is to be
     *            created
     * @return The negated matrix
     */
    public static Matrix4f negate(final Matrix4f src, Matrix4f dest)
    {
        if (dest == null) dest = new Matrix4f();

        dest.m00 = -src.m00;
        dest.m01 = -src.m01;
        dest.m02 = -src.m02;
        dest.m03 = -src.m03;
        dest.m10 = -src.m10;
        dest.m11 = -src.m11;
        dest.m12 = -src.m12;
        dest.m13 = -src.m13;
        dest.m20 = -src.m20;
        dest.m21 = -src.m21;
        dest.m22 = -src.m22;
        dest.m23 = -src.m23;
        dest.m30 = -src.m30;
        dest.m31 = -src.m31;
        dest.m32 = -src.m32;
        dest.m33 = -src.m33;

        return dest;
    }
}
