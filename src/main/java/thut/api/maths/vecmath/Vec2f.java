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
 * Holds a 2-tuple vector.
 *
 * @author cix_foo &lt;cix_foo@users.sourceforge.net&gt;
 * @version $Revision$
 *          $Id$
 */

public class Vec2f extends Vector implements Serializable
{

    private static final long serialVersionUID = 1L;

    public float x, y;

    /**
     * Constructor for Vector3f.
     */
    public Vec2f()
    {
        super();
    }

    /**
     * Constructor
     */
    public Vec2f(final float x, final float y)
    {
        this.set(x, y);
    }

    /*
     * (non-Javadoc)
     * @see org.lwjgl.util.vector.WritableVector2f#set(float, float)
     */
    public void set(final float x, final float y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the length squared of the vector
     */
    @Override
    public float lengthSquared()
    {
        return this.x * this.x + this.y * this.y;
    }

    /**
     * Translate a vector
     *
     * @param x
     *            The translation in x
     * @param y
     *            the translation in y
     * @return this
     */
    public Vec2f translate(final float x, final float y)
    {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Negate a vector
     *
     * @return this
     */
    @Override
    public Vector negate()
    {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    /**
     * Negate a vector and place the result in a destination vector.
     *
     * @param dest
     *            The destination vector or null if a new vector is to be
     *            created
     * @return the negated vector
     */
    public Vec2f negate(Vec2f dest)
    {
        if (dest == null) dest = new Vec2f();
        dest.x = -this.x;
        dest.y = -this.y;
        return dest;
    }

    /**
     * Normalise this vector and place the result in another vector.
     *
     * @param dest
     *            The destination vector, or null if a new vector is to be
     *            created
     * @return the normalised vector
     */
    public Vec2f normalise(Vec2f dest)
    {
        final float l = this.length();

        if (dest == null) dest = new Vec2f(this.x / l, this.y / l);
        else dest.set(this.x / l, this.y / l);

        return dest;
    }

    /**
     * The dot product of two vectors is calculated as
     * v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
     *
     * @param left
     *            The LHS vector
     * @param right
     *            The RHS vector
     * @return left dot right
     */
    public static float dot(final Vec2f left, final Vec2f right)
    {
        return left.x * right.x + left.y * right.y;
    }

    /**
     * Calculate the angle between two vectors, in radians
     *
     * @param a
     *            A vector
     * @param b
     *            The other vector
     * @return the angle between the two vectors, in radians
     */
    public static float angle(final Vec2f a, final Vec2f b)
    {
        float dls = Vec2f.dot(a, b) / (a.length() * b.length());
        if (dls < -1f) dls = -1f;
        else if (dls > 1.0f) dls = 1.0f;
        return (float) Math.acos(dls);
    }

    /**
     * Add a vector to another vector and place the result in a destination
     * vector.
     *
     * @param left
     *            The LHS vector
     * @param right
     *            The RHS vector
     * @param dest
     *            The destination vector, or null if a new vector is to be
     *            created
     * @return the sum of left and right in dest
     */
    public static Vec2f add(final Vec2f left, final Vec2f right, final Vec2f dest)
    {
        if (dest == null) return new Vec2f(left.x + right.x, left.y + right.y);
        else
        {
            dest.set(left.x + right.x, left.y + right.y);
            return dest;
        }
    }

    /**
     * Subtract a vector from another vector and place the result in a
     * destination
     * vector.
     *
     * @param left
     *            The LHS vector
     * @param right
     *            The RHS vector
     * @param dest
     *            The destination vector, or null if a new vector is to be
     *            created
     * @return left minus right in dest
     */
    public static Vec2f sub(final Vec2f left, final Vec2f right, final Vec2f dest)
    {
        if (dest == null) return new Vec2f(left.x - right.x, left.y - right.y);
        else
        {
            dest.set(left.x - right.x, left.y - right.y);
            return dest;
        }
    }

    /**
     * Store this vector in a FloatBuffer
     *
     * @param buf
     *            The buffer to store it in, at the current position
     * @return this
     */
    @Override
    public Vector store(final FloatBuffer buf)
    {
        buf.put(this.x);
        buf.put(this.y);
        return this;
    }

    /**
     * Load this vector from a FloatBuffer
     *
     * @param buf
     *            The buffer to load it from, at the current position
     * @return this
     */
    @Override
    public Vector load(final FloatBuffer buf)
    {
        this.x = buf.get();
        this.y = buf.get();
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.lwjgl.vector.Vector#scale(float)
     */
    @Override
    public Vector scale(final float scale)
    {

        this.x *= scale;
        this.y *= scale;

        return this;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(64);

        sb.append("Vector2f[");
        sb.append(this.x);
        sb.append(", ");
        sb.append(this.y);
        sb.append(']');
        return sb.toString();
    }

    /**
     * @return x
     */
    public final float getX()
    {
        return this.x;
    }

    /**
     * @return y
     */
    public final float getY()
    {
        return this.y;
    }

    /**
     * Set X
     *
     * @param x
     */
    public final void setX(final float x)
    {
        this.x = x;
    }

    /**
     * Set Y
     *
     * @param y
     */
    public final void setY(final float y)
    {
        this.y = y;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final Vec2f other = (Vec2f) obj;

        if (this.x == other.x && this.y == other.y) return true;

        return false;
    }

}
