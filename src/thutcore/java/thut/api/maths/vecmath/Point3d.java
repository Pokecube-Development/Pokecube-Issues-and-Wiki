package thut.api.maths.vecmath;

/**
 * A 3 element point that is represented by double precision floating point
 * x,y,z coordinates.
 */
public class Point3d extends Tuple3d implements java.io.Serializable
{

    // Compatible with 1.1
    static final long serialVersionUID = 5718062286069042927L;

    /**
     * Constructs and initializes a Point3d from the specified xyz coordinates.
     *
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param z
     *            the z coordinate
     */
    public Point3d(final double x, final double y, final double z)
    {
        super(x, y, z);
    }

    /**
     * Constructs and initializes a Point3d from the array of length 3.
     *
     * @param p
     *            the array of length 3 containing xyz in order
     */
    public Point3d(final double[] p)
    {
        super(p);
    }

    /**
     * Constructs and initializes a Point3d from the specified Point3d.
     *
     * @param p1
     *            the Point3d containing the initialization x y z data
     */
    public Point3d(final Point3d p1)
    {
        super(p1);
    }

    /**
     * Constructs and initializes a Point3d from the specified Point3f.
     *
     * @param p1
     *            the Point3f containing the initialization x y z data
     */
    public Point3d(final Point3f p1)
    {
        super(p1);
    }

    /**
     * Constructs and initializes a Point3d from the specified Tuple3f.
     *
     * @param t1
     *            the Tuple3f containing the initialization x y z data
     */
    public Point3d(final Tuple3f t1)
    {
        super(t1);
    }

    /**
     * Constructs and initializes a Point3d from the specified Tuple3d.
     *
     * @param t1
     *            the Tuple3d containing the initialization x y z data
     */
    public Point3d(final Tuple3d t1)
    {
        super(t1);
    }

    /**
     * Constructs and initializes a Point3d to (0,0,0).
     */
    public Point3d()
    {
        super();
    }

    /**
     * Returns the square of the distance between this point and point p1.
     *
     * @param p1
     *            the other point
     * @return the square of the distance
     */
    public final double distanceSquared(final Point3d p1)
    {
        double dx, dy, dz;

        dx = this.x - p1.x;
        dy = this.y - p1.y;
        dz = this.z - p1.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Returns the distance between this point and point p1.
     *
     * @param p1
     *            the other point
     * @return the distance
     */
    public final double distance(final Point3d p1)
    {
        double dx, dy, dz;

        dx = this.x - p1.x;
        dy = this.y - p1.y;
        dz = this.z - p1.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Computes the L-1 (Manhattan) distance between this point and
     * point p1. The L-1 distance is equal to:
     * abs(x1-x2) + abs(y1-y2) + abs(z1-z2).
     *
     * @param p1
     *            the other point
     * @return the L-1 distance
     */
    public final double distanceL1(final Point3d p1)
    {
        return Math.abs(this.x - p1.x) + Math.abs(this.y - p1.y) + Math.abs(this.z - p1.z);
    }

    /**
     * Computes the L-infinite distance between this point and
     * point p1. The L-infinite distance is equal to
     * MAX[abs(x1-x2), abs(y1-y2), abs(z1-z2)].
     *
     * @param p1
     *            the other point
     * @return the L-infinite distance
     */
    public final double distanceLinf(final Point3d p1)
    {
        double tmp;
        tmp = Math.max(Math.abs(this.x - p1.x), Math.abs(this.y - p1.y));

        return Math.max(tmp, Math.abs(this.z - p1.z));
    }

    /**
     * Multiplies each of the x,y,z components of the Point4d parameter
     * by 1/w and places the projected values into this point.
     *
     * @param p1
     *            the source Point4d, which is not modified
     */
    public final void project(final Point4d p1)
    {
        double oneOw;

        oneOw = 1 / p1.w;
        this.x = p1.x * oneOw;
        this.y = p1.y * oneOw;
        this.z = p1.z * oneOw;

    }

}
