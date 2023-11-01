package thut.api.maths.vecmath;

/**
 * A 4-element tuple represented by single-precision floating point x,y,z,w
 * coordinates.
 */
public abstract class Tuple4f implements java.io.Serializable, Cloneable
{

    static final long serialVersionUID = 7068460319248845763L;

    /**
     * The x coordinate.
     */
    public float x;

    /**
     * The y coordinate.
     */
    public float y;

    /**
     * The z coordinate.
     */
    public float z;

    /**
     * The w coordinate.
     */
    public float w;

    /**
     * Constructs and initializes a Tuple4f from the specified xyzw coordinates.
     *
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param z
     *            the z coordinate
     * @param w
     *            the w coordinate
     */
    public Tuple4f(final float x, final float y, final float z, final float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Constructs and initializes a Tuple4f from the array of length 4.
     *
     * @param t
     *            the array of length 4 containing xyzw in order
     */
    public Tuple4f(final float[] t)
    {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
        this.w = t[3];
    }

    /**
     * Constructs and initializes a Tuple4f from the specified Tuple4f.
     *
     * @param t1
     *            the Tuple4f containing the initialization x y z w data
     */
    public Tuple4f(final Tuple4f t1)
    {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = t1.w;
    }

    /**
     * Constructs and initializes a Tuple4f from the specified Tuple4d.
     *
     * @param t1
     *            the Tuple4d containing the initialization x y z w data
     */
    public Tuple4f(final Tuple4d t1)
    {
        this.x = (float) t1.x;
        this.y = (float) t1.y;
        this.z = (float) t1.z;
        this.w = (float) t1.w;
    }

    /**
     * Constructs and initializes a Tuple4f to (0,0,0,0).
     */
    public Tuple4f()
    {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
        this.w = 0.0f;
    }

    /**
     * Sets the value of this tuple to the specified xyzw coordinates.
     *
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param z
     *            the z coordinate
     * @param w
     *            the w coordinate
     */
    public final void set(final float x, final float y, final float z, final float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Sets the value of this tuple to the specified coordinates in the
     * array of length 4.
     *
     * @param t
     *            the array of length 4 containing xyzw in order
     */
    public final void set(final float[] t)
    {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
        this.w = t[3];
    }

    /**
     * Sets the value of this tuple to the value of tuple t1.
     *
     * @param t1
     *            the tuple to be copied
     */
    public final void set(final Tuple4f t1)
    {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = t1.w;
    }

    /**
     * Sets the value of this tuple to the value of tuple t1.
     *
     * @param t1
     *            the tuple to be copied
     */
    public final void set(final Tuple4d t1)
    {
        this.x = (float) t1.x;
        this.y = (float) t1.y;
        this.z = (float) t1.z;
        this.w = (float) t1.w;
    }

    /**
     * Copies the values of this tuple into the array t.
     *
     * @param t
     *            the array
     */
    public final void get(final float[] t)
    {
        t[0] = this.x;
        t[1] = this.y;
        t[2] = this.z;
        t[3] = this.w;
    }

    /**
     * Copies the values of this tuple into the tuple t.
     *
     * @param t
     *            the target tuple
     */
    public final void get(final Tuple4f t)
    {
        t.x = this.x;
        t.y = this.y;
        t.z = this.z;
        t.w = this.w;
    }

    /**
     * Sets the value of this tuple to the sum of tuples t1 and t2.
     *
     * @param t1
     *            the first tuple
     * @param t2
     *            the second tuple
     */
    public final void add(final Tuple4f t1, final Tuple4f t2)
    {
        this.x = t1.x + t2.x;
        this.y = t1.y + t2.y;
        this.z = t1.z + t2.z;
        this.w = t1.w + t2.w;
    }

    /**
     * Sets the value of this tuple to the sum of itself and t1.
     *
     * @param t1
     *            the other tuple
     */
    public final void add(final Tuple4f t1)
    {
        this.x += t1.x;
        this.y += t1.y;
        this.z += t1.z;
        this.w += t1.w;
    }

    /**
     * Sets the value of this tuple to the difference
     * of tuples t1 and t2 (this = t1 - t2).
     *
     * @param t1
     *            the first tuple
     * @param t2
     *            the second tuple
     */
    public final void sub(final Tuple4f t1, final Tuple4f t2)
    {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
        this.z = t1.z - t2.z;
        this.w = t1.w - t2.w;
    }

    /**
     * Sets the value of this tuple to the difference
     * of itself and t1 (this = this - t1).
     *
     * @param t1
     *            the other tuple
     */
    public final void sub(final Tuple4f t1)
    {
        this.x -= t1.x;
        this.y -= t1.y;
        this.z -= t1.z;
        this.w -= t1.w;
    }

    /**
     * Sets the value of this tuple to the negation of tuple t1.
     *
     * @param t1
     *            the source tuple
     */
    public final void negate(final Tuple4f t1)
    {
        this.x = -t1.x;
        this.y = -t1.y;
        this.z = -t1.z;
        this.w = -t1.w;
    }

    /**
     * Negates the value of this tuple in place.
     */
    public final void negate()
    {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1.
     *
     * @param s
     *            the scalar value
     * @param t1
     *            the source tuple
     */
    public final void scale(final float s, final Tuple4f t1)
    {
        this.x = s * t1.x;
        this.y = s * t1.y;
        this.z = s * t1.z;
        this.w = s * t1.w;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of the scale factor with this.
     *
     * @param s
     *            the scalar value
     */
    public final void scale(final float s)
    {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        this.w *= s;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1 plus tuple t2 (this = s*t1 + t2).
     *
     * @param s
     *            the scalar value
     * @param t1
     *            the tuple to be multipled
     * @param t2
     *            the tuple to be added
     */
    public final void scaleAdd(final float s, final Tuple4f t1, final Tuple4f t2)
    {
        this.x = s * t1.x + t2.x;
        this.y = s * t1.y + t2.y;
        this.z = s * t1.z + t2.z;
        this.w = s * t1.w + t2.w;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of itself and then adds tuple t1 (this = s*this + t1).
     *
     * @param s
     *            the scalar value
     * @param t1
     *            the tuple to be added
     */
    public final void scaleAdd(final float s, final Tuple4f t1)
    {
        this.x = s * this.x + t1.x;
        this.y = s * this.y + t1.y;
        this.z = s * this.z + t1.z;
        this.w = s * this.w + t1.w;
    }

    /**
     * Returns a string that contains the values of this Tuple4f.
     * The form is (x,y,z,w).
     *
     * @return the String representation
     */
    @Override
    public String toString()
    {
        return "(" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ")";
    }

    /**
     * Returns true if all of the data members of Tuple4f t1 are
     * equal to the corresponding data members in this Tuple4f.
     *
     * @param t1
     *            the vector with which the comparison is made
     * @return true or false
     */
    public boolean equals(final Tuple4f t1)
    {
        try
        {
            return this.x == t1.x && this.y == t1.y && this.z == t1.z && this.w == t1.w;
        }
        catch (final NullPointerException e2)
        {
            return false;
        }
    }

    /**
     * Returns true if the Object t1 is of type Tuple4f and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Tuple4f.
     *
     * @param t1
     *            the object with which the comparison is made
     * @return true or false
     */
    @Override
    public boolean equals(final Object t1)
    {
        try
        {
            final Tuple4f t2 = (Tuple4f) t1;
            return this.x == t2.x && this.y == t2.y && this.z == t2.z && this.w == t2.w;
        }
        catch (final NullPointerException e2)
        {
            return false;
        }
        catch (final ClassCastException e1)
        {
            return false;
        }
    }

    /**
     * Returns true if the L-infinite distance between this tuple
     * and tuple t1 is less than or equal to the epsilon parameter,
     * otherwise returns false. The L-infinite
     * distance is equal to
     * MAX[abs(x1-x2), abs(y1-y2), abs(z1-z2), abs(w1-w2)].
     *
     * @param t1
     *            the tuple to be compared to this tuple
     * @param epsilon
     *            the threshold value
     * @return true or false
     */
    public boolean epsilonEquals(final Tuple4f t1, final float epsilon)
    {
        float diff;

        diff = this.x - t1.x;
        if (Float.isNaN(diff)) return false;
        if ((diff < 0 ? -diff : diff) > epsilon) return false;

        diff = this.y - t1.y;
        if (Float.isNaN(diff)) return false;
        if ((diff < 0 ? -diff : diff) > epsilon) return false;

        diff = this.z - t1.z;
        if (Float.isNaN(diff)) return false;
        if ((diff < 0 ? -diff : diff) > epsilon) return false;

        diff = this.w - t1.w;
        if (Float.isNaN(diff)) return false;
        if ((diff < 0 ? -diff : diff) > epsilon) return false;

        return true;
    }

    /**
     * Returns a hash code value based on the data values in this
     * object. Two different Tuple4f objects with identical data values
     * (i.e., Tuple4f.equals returns true) will return the same hash
     * code value. Two objects with different data members may return the
     * same hash value, although this is not likely.
     *
     * @return the integer hash code value
     */
    @Override
    public int hashCode()
    {
        long bits = 1L;
        bits = 31L * bits + VecMathUtil.floatToIntBits(this.x);
        bits = 31L * bits + VecMathUtil.floatToIntBits(this.y);
        bits = 31L * bits + VecMathUtil.floatToIntBits(this.z);
        bits = 31L * bits + VecMathUtil.floatToIntBits(this.w);
        return (int) (bits ^ bits >> 32);
    }

    /**
     * Clamps the tuple parameter to the range [low, high] and
     * places the values into this tuple.
     *
     * @param min
     *            the lowest value in the tuple after clamping
     * @param max
     *            the highest value in the tuple after clamping
     * @param t
     *            the source tuple, which will not be modified
     */
    public final void clamp(final float min, final float max, final Tuple4f t)
    {
        if (t.x > max) this.x = max;
        else if (t.x < min) this.x = min;
        else this.x = t.x;

        if (t.y > max) this.y = max;
        else if (t.y < min) this.y = min;
        else this.y = t.y;

        if (t.z > max) this.z = max;
        else if (t.z < min) this.z = min;
        else this.z = t.z;

        if (t.w > max) this.w = max;
        else if (t.w < min) this.w = min;
        else this.w = t.w;

    }

    /**
     * Clamps the minimum value of the tuple parameter to the min
     * parameter and places the values into this tuple.
     *
     * @param min
     *            the lowest value in the tuple after clamping
     * @param t
     *            the source tuple, which will not be modified
     */
    public final void clampMin(final float min, final Tuple4f t)
    {
        if (t.x < min) this.x = min;
        else this.x = t.x;

        if (t.y < min) this.y = min;
        else this.y = t.y;

        if (t.z < min) this.z = min;
        else this.z = t.z;

        if (t.w < min) this.w = min;
        else this.w = t.w;

    }

    /**
     * Clamps the maximum value of the tuple parameter to the max
     * parameter and places the values into this tuple.
     *
     * @param max
     *            the highest value in the tuple after clamping
     * @param t
     *            the source tuple, which will not be modified
     */
    public final void clampMax(final float max, final Tuple4f t)
    {
        if (t.x > max) this.x = max;
        else this.x = t.x;

        if (t.y > max) this.y = max;
        else this.y = t.y;

        if (t.z > max) this.z = max;
        else this.z = t.z;

        if (t.w > max) this.w = max;
        else this.w = t.z;

    }

    /**
     * Sets each component of the tuple parameter to its absolute
     * value and places the modified values into this tuple.
     *
     * @param t
     *            the source tuple, which will not be modified
     */
    public final void absolute(final Tuple4f t)
    {
        this.x = Math.abs(t.x);
        this.y = Math.abs(t.y);
        this.z = Math.abs(t.z);
        this.w = Math.abs(t.w);
    }

    /**
     * Clamps this tuple to the range [low, high].
     *
     * @param min
     *            the lowest value in this tuple after clamping
     * @param max
     *            the highest value in this tuple after clamping
     */
    public final void clamp(final float min, final float max)
    {
        if (this.x > max) this.x = max;
        else if (this.x < min) this.x = min;

        if (this.y > max) this.y = max;
        else if (this.y < min) this.y = min;

        if (this.z > max) this.z = max;
        else if (this.z < min) this.z = min;

        if (this.w > max) this.w = max;
        else if (this.w < min) this.w = min;

    }

    /**
     * Clamps the minimum value of this tuple to the min parameter.
     *
     * @param min
     *            the lowest value in this tuple after clamping
     */
    public final void clampMin(final float min)
    {
        if (this.x < min) this.x = min;
        if (this.y < min) this.y = min;
        if (this.z < min) this.z = min;
        if (this.w < min) this.w = min;

    }

    /**
     * Clamps the maximum value of this tuple to the max parameter.
     *
     * @param max
     *            the highest value in the tuple after clamping
     */
    public final void clampMax(final float max)
    {
        if (this.x > max) this.x = max;
        if (this.y > max) this.y = max;
        if (this.z > max) this.z = max;
        if (this.w > max) this.w = max;

    }

    /**
     * Sets each component of this tuple to its absolute value.
     */
    public final void absolute()
    {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        this.w = Math.abs(this.w);
    }

    /**
     * Linearly interpolates between tuples t1 and t2 and places the
     * result into this tuple: this = (1-alpha)*t1 + alpha*t2.
     *
     * @param t1
     *            the first tuple
     * @param t2
     *            the second tuple
     * @param alpha
     *            the alpha interpolation parameter
     */
    public void interpolate(final Tuple4f t1, final Tuple4f t2, final float alpha)
    {
        this.x = (1 - alpha) * t1.x + alpha * t2.x;
        this.y = (1 - alpha) * t1.y + alpha * t2.y;
        this.z = (1 - alpha) * t1.z + alpha * t2.z;
        this.w = (1 - alpha) * t1.w + alpha * t2.w;

    }

    /**
     * Linearly interpolates between this tuple and tuple t1 and
     * places the result into this tuple: this = (1-alpha)*this + alpha*t1.
     *
     * @param t1
     *            the first tuple
     * @param alpha
     *            the alpha interpolation parameter
     */
    public void interpolate(final Tuple4f t1, final float alpha)
    {
        this.x = (1 - alpha) * this.x + alpha * t1.x;
        this.y = (1 - alpha) * this.y + alpha * t1.y;
        this.z = (1 - alpha) * this.z + alpha * t1.z;
        this.w = (1 - alpha) * this.w + alpha * t1.w;

    }

    /**
     * Creates a new object of the same class as this object.
     *
     * @return a clone of this instance.
     * @exception OutOfMemoryError
     *                if there is not enough memory.
     * @see java.lang.Cloneable
     * @since vecmath 1.3
     */
    @Override
    public Object clone()
    {
        // Since there are no arrays we can just use Object.clone()
        try
        {
            return super.clone();
        }
        catch (final CloneNotSupportedException e)
        {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Get the <i>x</i> coordinate.
     *
     * @return the <i>x</i> coordinate.
     * @since vecmath 1.5
     */
    public final float getX()
    {
        return this.x;
    }

    /**
     * Set the <i>x</i> coordinate.
     *
     * @param x
     *            value to <i>x</i> coordinate.
     * @since vecmath 1.5
     */
    public final void setX(final float x)
    {
        this.x = x;
    }

    /**
     * Get the <i>y</i> coordinate.
     *
     * @return the <i>y</i> coordinate.
     * @since vecmath 1.5
     */
    public final float getY()
    {
        return this.y;
    }

    /**
     * Set the <i>y</i> coordinate.
     *
     * @param y
     *            value to <i>y</i> coordinate.
     * @since vecmath 1.5
     */
    public final void setY(final float y)
    {
        this.y = y;
    }

    /**
     * Get the <i>z</i> coordinate.
     *
     * @return the <i>z</i> coordinate.
     * @since vecmath 1.5
     */
    public final float getZ()
    {
        return this.z;
    }

    /**
     * Set the <i>z</i> coordinate.
     *
     * @param z
     *            value to <i>z</i> coordinate.
     * @since vecmath 1.5
     */
    public final void setZ(final float z)
    {
        this.z = z;
    }

    /**
     * Get the <i>w</i> coordinate.
     *
     * @return the <i>w</i> coordinate.
     * @since vecmath 1.5
     */
    public final float getW()
    {
        return this.w;
    }

    /**
     * Set the <i>w</i> coordinate.
     *
     * @param w
     *            value to <i>w</i> coordinate.
     * @since vecmath 1.5
     */
    public final void setW(final float w)
    {
        this.w = w;
    }
}
