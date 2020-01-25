package thut.api.maths.vecmath;

/**
 * A 4-element vector represented by single-precision floating point x,y,z,w
 * coordinates.
 */
public class Vector4f extends Tuple4f implements java.io.Serializable
{
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
    public static Vector4f add(final Vector4f left, final Vector4f right, final Vector4f dest)
    {
        if (dest == null) return new Vector4f(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
        else
        {
            dest.set(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
            return dest;
        }
    }

    // Compatible with 1.1
    static final long serialVersionUID = 8749319902347760659L;

    /**
     * Constructs and initializes a Vector4f from the specified xyzw
     * coordinates.
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
    public Vector4f(final float x, final float y, final float z, final float w)
    {
        super(x, y, z, w);
    }

    /**
     * Constructs and initializes a Vector4f from the array of length 4.
     *
     * @param v
     *            the array of length 4 containing xyzw in order
     */
    public Vector4f(final float[] v)
    {
        super(v);
    }

    /**
     * Constructs and initializes a Vector4f from the specified Vector4f.
     *
     * @param v1
     *            the Vector4f containing the initialization x y z w data
     */
    public Vector4f(final Vector4f v1)
    {
        super(v1);
    }

    /**
     * Constructs and initializes a Vector4f from the specified Vector4d.
     *
     * @param v1
     *            the Vector4d containing the initialization x y z w data
     */
    public Vector4f(final Vector4d v1)
    {
        super(v1);
    }

    /**
     * Constructs and initializes a Vector4f from the specified Tuple4f.
     *
     * @param t1
     *            the Tuple4f containing the initialization x y z w data
     */
    public Vector4f(final Tuple4f t1)
    {
        super(t1);
    }

    /**
     * Constructs and initializes a Vector4f from the specified Tuple4d.
     *
     * @param t1
     *            the Tuple4d containing the initialization x y z w data
     */
    public Vector4f(final Tuple4d t1)
    {
        super(t1);
    }

    /**
     * Constructs and initializes a Vector4f from the specified Tuple3f.
     * The x,y,z components of this vector are set to the corresponding
     * components of tuple t1. The w component of this vector
     * is set to 0.
     *
     * @param t1
     *            the tuple to be copied
     * @since vecmath 1.2
     */
    public Vector4f(final Tuple3f t1)
    {
        super(t1.x, t1.y, t1.z, 0.0f);
    }

    /**
     * Constructs and initializes a Vector4f to (0,0,0,0).
     */
    public Vector4f()
    {
        super();
    }

    /**
     * Sets the x,y,z components of this vector to the corresponding
     * components of tuple t1. The w component of this vector
     * is set to 0.
     *
     * @param t1
     *            the tuple to be copied
     * @since vecmath 1.2
     */
    public final void set(final Tuple3f t1)
    {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = 0.0f;
    }

    /**
     * Returns the length of this vector.
     *
     * @return the length of this vector as a float
     */
    public final float length()
    {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w);
    }

    /**
     * Returns the squared length of this vector
     *
     * @return the squared length of this vector as a float
     */
    public final float lengthSquared()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    /**
     * returns the dot product of this vector and v1
     *
     * @param v1
     *            the other vector
     * @return the dot product of this vector and v1
     */
    public final float dot(final Vector4f v1)
    {
        return this.x * v1.x + this.y * v1.y + this.z * v1.z + this.w * v1.w;
    }

    /**
     * Sets the value of this vector to the normalization of vector v1.
     *
     * @param v1
     *            the un-normalized vector
     */
    public final void normalize(final Vector4f v1)
    {
        float norm;

        norm = (float) (1.0 / Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z + v1.w * v1.w));
        this.x = v1.x * norm;
        this.y = v1.y * norm;
        this.z = v1.z * norm;
        this.w = v1.w * norm;
    }

    /**
     * Normalizes this vector in place.
     */
    public final void normalize()
    {
        float norm;

        norm = (float) (1.0 / Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w));
        this.x *= norm;
        this.y *= norm;
        this.z *= norm;
        this.w *= norm;
    }

    /**
     * Returns the (4-space) angle in radians between this vector and
     * the vector parameter; the return value is constrained to the
     * range [0,PI].
     *
     * @param v1
     *            the other vector
     * @return the angle in radians in the range [0,PI]
     */
    public final float angle(final Vector4f v1)
    {
        double vDot = this.dot(v1) / (this.length() * v1.length());
        if (vDot < -1.0) vDot = -1.0;
        if (vDot > 1.0) vDot = 1.0;
        return (float) Math.acos(vDot);
    }

}
