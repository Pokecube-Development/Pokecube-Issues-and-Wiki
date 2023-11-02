package thut.api.maths.vecmath;

/**
 * A generic 3-element tuple that is represented by single precision-floating
 * point x,y,z coordinates.
 */
public abstract class Tuple3f implements java.io.Serializable, Cloneable
{

    static final long serialVersionUID = 5019834619484343712L;

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
     * Constructs and initializes a Tuple3f from the specified xyz coordinates.
     *
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param z
     *            the z coordinate
     */
    public Tuple3f(final float x, final float y, final float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructs and initializes a Tuple3f from the array of length 3.
     *
     * @param t
     *            the array of length 3 containing xyz in order
     */
    public Tuple3f(final float[] t)
    {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
    }

    /**
     * Constructs and initializes a Tuple3f from the specified Tuple3f.
     *
     * @param t1
     *            the Tuple3f containing the initialization x y z data
     */
    public Tuple3f(final Tuple3f t1)
    {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
    }

    /**
     * Constructs and initializes a Tuple3f from the specified Tuple3d.
     *
     * @param t1
     *            the Tuple3d containing the initialization x y z data
     */
    public Tuple3f(final Tuple3d t1)
    {
        this.x = (float) t1.x;
        this.y = (float) t1.y;
        this.z = (float) t1.z;
    }

    /**
     * Constructs and initializes a Tuple3f to (0,0,0).
     */
    public Tuple3f()
    {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }

    /**
     * Returns a string that contains the values of this Tuple3f.
     * The form is (x,y,z).
     *
     * @return the String representation
     */
    @Override
    public String toString()
    {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    /**
     * Sets the value of this tuple to the specified xyz coordinates.
     *
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param z
     *            the z coordinate
     */
    public final void set(final float x, final float y, final float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Sets the value of this tuple to the xyz coordinates specified in
     * the array of length 3.
     *
     * @param t
     *            the array of length 3 containing xyz in order
     */
    public final void set(final float[] t)
    {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
    }

    /**
     * Sets the value of this tuple to the value of tuple t1.
     *
     * @param t1
     *            the tuple to be copied
     */
    public final void set(final Tuple3f t1)
    {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
    }

    /**
     * Sets the value of this tuple to the value of tuple t1.
     *
     * @param t1
     *            the tuple to be copied
     */
    public final void set(final Tuple3d t1)
    {
        this.x = (float) t1.x;
        this.y = (float) t1.y;
        this.z = (float) t1.z;
    }

    /**
     * Gets the value of this tuple and copies the values into t.
     *
     * @param t
     *            the array of length 3 into which the values are copied
     */
    public final void get(final float[] t)
    {
        t[0] = this.x;
        t[1] = this.y;
        t[2] = this.z;
    }

    /**
     * Gets the value of this tuple and copies the values into t.
     *
     * @param t
     *            the Tuple3f object into which the values of this object are
     *            copied
     */
    public final void get(final Tuple3f t)
    {
        t.x = this.x;
        t.y = this.y;
        t.z = this.z;
    }

    /**
     * Sets the value of this tuple to the vector sum of tuples t1 and t2.
     *
     * @param t1
     *            the first tuple
     * @param t2
     *            the second tuple
     */
    public final void add(final Tuple3f t1, final Tuple3f t2)
    {
        this.x = t1.x + t2.x;
        this.y = t1.y + t2.y;
        this.z = t1.z + t2.z;
    }

    /**
     * Sets the value of this tuple to the vector sum of itself and tuple t1.
     *
     * @param t1
     *            the other tuple
     */
    public final void add(final Tuple3f t1)
    {
        this.x += t1.x;
        this.y += t1.y;
        this.z += t1.z;
    }

    /**
     * Sets the value of this tuple to the vector difference
     * of tuples t1 and t2 (this = t1 - t2).
     *
     * @param t1
     *            the first tuple
     * @param t2
     *            the second tuple
     */
    public final void sub(final Tuple3f t1, final Tuple3f t2)
    {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
        this.z = t1.z - t2.z;
    }

    /**
     * Sets the value of this tuple to the vector difference of
     * itself and tuple t1 (this = this - t1) .
     *
     * @param t1
     *            the other tuple
     */
    public final void sub(final Tuple3f t1)
    {
        this.x -= t1.x;
        this.y -= t1.y;
        this.z -= t1.z;
    }

    /**
     * Sets the value of this tuple to the negation of tuple t1.
     *
     * @param t1
     *            the source tuple
     */
    public final void negate(final Tuple3f t1)
    {
        this.x = -t1.x;
        this.y = -t1.y;
        this.z = -t1.z;
    }

    /**
     * Negates the value of this tuple in place.
     */
    public final void negate()
    {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }

    /**
     * Sets the value of this vector to the scalar multiplication
     * of tuple t1.
     *
     * @param s
     *            the scalar value
     * @param t1
     *            the source tuple
     */
    public final void scale(final float s, final Tuple3f t1)
    {
        this.x = s * t1.x;
        this.y = s * t1.y;
        this.z = s * t1.z;
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
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1 and then adds tuple t2 (this = s*t1 + t2).
     *
     * @param s
     *            the scalar value
     * @param t1
     *            the tuple to be scaled and added
     * @param t2
     *            the tuple to be added without a scale
     */
    public final void scaleAdd(final float s, final Tuple3f t1, final Tuple3f t2)
    {
        this.x = s * t1.x + t2.x;
        this.y = s * t1.y + t2.y;
        this.z = s * t1.z + t2.z;
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
    public final void scaleAdd(final float s, final Tuple3f t1)
    {
        this.x = s * this.x + t1.x;
        this.y = s * this.y + t1.y;
        this.z = s * this.z + t1.z;
    }

    /**
     * Returns true if the Object t1 is of type Tuple3f and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Tuple3f.
     *
     * @param t1
     *            the vector with which the comparison is made
     * @return true or false
     */
    public boolean equals(final Tuple3f t1)
    {
        try
        {
            return this.x == t1.x && this.y == t1.y && this.z == t1.z;
        }
        catch (final NullPointerException e2)
        {
            return false;
        }
    }

    /**
     * Returns true if the Object t1 is of type Tuple3f and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Tuple3f.
     *
     * @param t1
     *            the Object with which the comparison is made
     * @return true or false
     */
    @Override
    public boolean equals(final Object t1)
    {
        try
        {
            final Tuple3f t2 = (Tuple3f) t1;
            return this.x == t2.x && this.y == t2.y && this.z == t2.z;
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
     * distance is equal to MAX[abs(x1-x2), abs(y1-y2), abs(z1-z2)].
     *
     * @param t1
     *            the tuple to be compared to this tuple
     * @param epsilon
     *            the threshold value
     * @return true or false
     */
    public boolean epsilonEquals(final Tuple3f t1, final float epsilon)
    {
        float diff;

        diff = Math.abs(this.x - t1.x);
        if (Float.isNaN(diff)) return false;
        if (diff > epsilon) return false;

        diff = Math.abs(this.y - t1.y);
        if (Float.isNaN(diff)) return false;
        if (diff > epsilon) return false;

        diff = Math.abs(this.z - t1.z);
        if (Float.isNaN(diff)) return false;
        if (diff > epsilon) return false;

        return true;

    }

    /**
     * Returns a hash code value based on the data values in this
     * object. Two different Tuple3f objects with identical data values
     * (i.e., Tuple3f.equals returns true) will return the same hash
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
    public final void clamp(final float min, final float max, final Tuple3f t)
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
    public final void clampMin(final float min, final Tuple3f t)
    {
        if (t.x < min) this.x = min;
        else this.x = t.x;

        if (t.y < min) this.y = min;
        else this.y = t.y;

        if (t.z < min) this.z = min;
        else this.z = t.z;

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
    public final void clampMax(final float max, final Tuple3f t)
    {
        if (t.x > max) this.x = max;
        else this.x = t.x;

        if (t.y > max) this.y = max;
        else this.y = t.y;

        if (t.z > max) this.z = max;
        else this.z = t.z;

    }

    /**
     * Sets each component of the tuple parameter to its absolute
     * value and places the modified values into this tuple.
     *
     * @param t
     *            the source tuple, which will not be modified
     */
    public final void absolute(final Tuple3f t)
    {
        this.x = Math.abs(t.x);
        this.y = Math.abs(t.y);
        this.z = Math.abs(t.z);
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

    }

    /**
     * Sets each component of this tuple to its absolute value.
     */
    public final void absolute()
    {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);

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
    public final void interpolate(final Tuple3f t1, final Tuple3f t2, final float alpha)
    {
        this.x = (1 - alpha) * t1.x + alpha * t2.x;
        this.y = (1 - alpha) * t1.y + alpha * t2.y;
        this.z = (1 - alpha) * t1.z + alpha * t2.z;

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
    public final void interpolate(final Tuple3f t1, final float alpha)
    {
        this.x = (1 - alpha) * this.x + alpha * t1.x;
        this.y = (1 - alpha) * this.y + alpha * t1.y;
        this.z = (1 - alpha) * this.z + alpha * t1.z;

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
     * @return the <i>z</i> coordinate
     * @since vecmath 1.5
     */
    public final float getZ()
    {
        return this.z;
    }

    /**
     * Set the <i>Z</i> coordinate.
     *
     * @param z
     *            value to <i>z</i> coordinate.
     * @since vecmath 1.5
     */
    public final void setZ(final float z)
    {
        this.z = z;
    }
}