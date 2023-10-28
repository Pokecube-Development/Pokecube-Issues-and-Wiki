package thut.api.maths.vecmath;

import thut.api.maths.Vector3;

/**
 * A 3-element vector that is represented by single-precision floating point
 * x,y,z coordinates. If this value represents a normal, then it should
 * be normalized.
 */
public class Vec3f extends Tuple3f implements java.io.Serializable
{
    public static Vec3f XN = new Vec3f(-1.0F, 0.0F, 0.0F);
    public static Vec3f XP = new Vec3f(1.0F, 0.0F, 0.0F);
    public static Vec3f YN = new Vec3f(0.0F, -1.0F, 0.0F);
    public static Vec3f YP = new Vec3f(0.0F, 1.0F, 0.0F);
    public static Vec3f ZN = new Vec3f(0.0F, 0.0F, -1.0F);
    public static Vec3f ZP = new Vec3f(0.0F, 0.0F, 1.0F);

    // Combatible with 1.1
    static final long serialVersionUID = -7031930069184524614L;

    /**
     * Constructs and initializes a Vector3f from the specified xyz coordinates.
     *
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param z
     *            the z coordinate
     */
    public Vec3f(final float x, final float y, final float z)
    {
        super(x, y, z);
    }

    /**
     * Constructs and initializes a Vector3f from the array of length 3.
     *
     * @param v
     *            the array of length 3 containing xyz in order
     */
    public Vec3f(final float[] v)
    {
        super(v);
    }

    /**
     * Constructs and initializes a Vector3f from the specified Vector3f.
     *
     * @param v1
     *            the Vector3f containing the initialization x y z data
     */
    public Vec3f(final Vec3f v1)
    {
        super(v1);
    }

    /**
     * Constructs and initializes a Vector3f from the specified Vector3d.
     *
     * @param v1
     *            the Vector3d containing the initialization x y z data
     */
    public Vec3f(final Vector3d v1)
    {
        super(v1);
    }

    /**
     * Constructs and initializes a Vector3f from the specified Tuple3f.
     *
     * @param t1
     *            the Tuple3f containing the initialization x y z data
     */
    public Vec3f(final Tuple3f t1)
    {
        super(t1);
    }

    /**
     * Constructs and initializes a Vector3f from the specified Tuple3d.
     *
     * @param t1
     *            the Tuple3d containing the initialization x y z data
     */
    public Vec3f(final Tuple3d t1)
    {
        super(t1);
    }

    /**
     * Constructs and initializes a Vector3f to (0,0,0).
     */
    public Vec3f()
    {
        super();
    }

    public Quat4f rotation(final float valueIn)
    {
        return new Quat4f(this, valueIn, false);
    }

    public Quat4f rotationDegrees(final float valueIn)
    {
        return new Quat4f(this, valueIn, true);
    }

    public com.mojang.math.Vector3f toMC()
    {
        return new com.mojang.math.Vector3f(this.x, this.y, this.z);
    }

    public Vec3f(final Vector3 rHat)
    {
        this((float) rHat.x, (float) rHat.y, (float) rHat.z);
    }

    public final void set(final Vector3 rHat)
    {
        this.x = (float) rHat.x;
        this.y = (float) rHat.y;
        this.z = (float) rHat.z;
    }

    /**
     * Returns the squared length of this vector.
     *
     * @return the squared length of this vector
     */
    public final float lengthSquared()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    /**
     * Returns the length of this vector.
     *
     * @return the length of this vector
     */
    public final float length()
    {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    /**
     * Sets this vector to be the vector cross product of vectors v1 and v2.
     *
     * @param v1
     *            the first vector
     * @param v2
     *            the second vector
     */
    public final void cross(final Vec3f v1, final Vec3f v2)
    {
        float x, y;

        x = v1.y * v2.z - v1.z * v2.y;
        y = v2.x * v1.z - v2.z * v1.x;
        this.z = v1.x * v2.y - v1.y * v2.x;
        this.x = x;
        this.y = y;
    }

    /**
     * Computes the dot product of this vector and vector v1.
     *
     * @param v1
     *            the other vector
     * @return the dot product of this vector and v1
     */
    public final float dot(final Vec3f v1)
    {
        return this.x * v1.x + this.y * v1.y + this.z * v1.z;
    }

    /**
     * Sets the value of this vector to the normalization of vector v1.
     *
     * @param v1
     *            the un-normalized vector
     */
    public final void normalize(final Vec3f v1)
    {
        float norm;

        norm = (float) (1.0 / Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z));
        this.x = v1.x * norm;
        this.y = v1.y * norm;
        this.z = v1.z * norm;
    }

    /**
     * Normalizes this vector in place.
     */
    public final void normalize()
    {
        float norm;

        norm = (float) (1.0 / Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z));
        this.x *= norm;
        this.y *= norm;
        this.z *= norm;
    }

    /**
     * Returns the angle in radians between this vector and the vector
     * parameter; the return value is constrained to the range [0,PI].
     *
     * @param v1
     *            the other vector
     * @return the angle in radians in the range [0,PI]
     */
    public final float angle(final Vec3f v1)
    {
        double vDot = this.dot(v1) / (this.length() * v1.length());
        if (vDot < -1.0) vDot = -1.0;
        if (vDot > 1.0) vDot = 1.0;
        return (float) Math.acos(vDot);
    }
}
