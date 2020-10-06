package thut.api.maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;

public class Matrix3
{

    private static boolean containsOrigin(final List<Vector3> points)
    {
        int index = 0;
        Vector3 base = points.get(index);
        double dist = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++)
        {
            final Vector3 v = points.get(i);
            if (v == null) // new Exception().printStackTrace();
                continue;
            final double d = v.magSq();
            if (d < dist)
            {
                base = points.get(i);
                dist = d;
                index = i;
            }
        }

        final Vector3 mid = Vector3.findMidPoint(points);
        points.remove(index);
        boolean ret = false;
        for (final Vector3 v : points)
        {
            if (v == null) // new Exception().printStackTrace();
                continue;
            final double d = v.dot(base);
            final double d1 = v.dot(mid);

            if (d <= 0) if (d1 <= d && Math.signum(d) == Math.signum(d1))
            {
                ret = true;
                return true;
            }

        }
        return ret;
    }

    public static AxisAlignedBB copyAndChange(final AxisAlignedBB box, final int index, final double value)
    {
        double x1 = box.minX;
        double x2 = box.maxX;
        double y1 = box.minY;
        double y2 = box.maxY;
        double z1 = box.minZ;
        double z2 = box.maxZ;
        if (index == 0) x1 = value;
        if (index == 1) y1 = value;
        if (index == 2) z1 = value;
        if (index == 3) x2 = value;
        if (index == 4) y2 = value;
        if (index == 5) z2 = value;

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Fills temp1 with the offsets
     *
     * @param aabbs
     * @param entityBox
     * @param e
     * @param diffs
     * @param temp1
     * @return
     */
    public static boolean doCollision(final List<AxisAlignedBB> aabbs, final AxisAlignedBB entityBox, final Entity e,
            final double yShift, final Vector3 diffs, final Vector3 temp1)
    {
        final double minX = entityBox.minX;
        final double minY = entityBox.minY;
        final double minZ = entityBox.minZ;
        final double maxX = entityBox.maxX;
        final double maxY = entityBox.maxY;
        final double maxZ = entityBox.maxZ;
        final double factor = 0.75d;
        final Vec3d motion = e.getMotion();
        double dx = Math.max(maxX - minX, 0.5) / factor + motion.x, dz = Math.max(maxZ - minZ, 0.5) / factor + motion.z,
                r;

        final boolean collide = false;
        final AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        Matrix3.mergeAABBs(aabbs, maxX - minX, maxY - minY, maxZ - minZ);

        final double yTop = Math.min(e.stepHeight + e.getPosY() + yShift, maxY);

        boolean floor = false;
        boolean ceiling = false;
        double yMaxFloor = minY;

        for (final AxisAlignedBB aabb : aabbs)
        {
            dx = 10e3;
            dz = 10e3;
            boolean thisFloor = false;
            boolean thisCieling = false;
            boolean collidesX = maxZ <= aabb.maxZ && maxZ >= aabb.minZ || minZ <= aabb.maxZ && minZ >= aabb.minZ
                    || minZ <= aabb.minZ && maxZ >= aabb.maxZ;

            final boolean collidesY = minY >= aabb.minY && minY <= aabb.maxY || maxY <= aabb.maxY && maxY >= aabb.minY
                    || minY <= aabb.minY && maxY >= aabb.maxY;

            boolean collidesZ = maxX <= aabb.maxX && maxX >= aabb.minX || minX <= aabb.maxX && minX >= aabb.minX
                    || minX <= aabb.minX && maxX >= aabb.maxX;

            collidesZ = collidesZ && (collidesX || collidesY);
            collidesX = collidesX && (collidesZ || collidesY);

            if (collidesX && collidesZ && yTop >= aabb.maxY && boundingBox.minY - e.stepHeight - yShift <= aabb.maxY
                    - diffs.y)
            {
                if (!floor) temp1.y = Math.max(aabb.maxY - boundingBox.minY, temp1.y);
                floor = true;
                thisFloor = aabb.maxY >= yMaxFloor;
                if (thisFloor) yMaxFloor = aabb.maxY;
            }
            if (collidesX && collidesZ && boundingBox.maxY >= aabb.minY && boundingBox.minY < aabb.minY)
            {
                if (!(floor || ceiling))
                {
                    final double dy = aabb.minY - boundingBox.maxY - diffs.y;
                    temp1.y = Math.min(dy, temp1.y);
                }
                thisCieling = !(thisFloor || floor);
                ceiling = true;
            }

            final boolean vert = thisFloor || thisCieling;

            if (collidesX && !vert && collidesY && boundingBox.maxX >= aabb.maxX && boundingBox.minX <= aabb.maxX)
            {
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesX && !vert && collidesY && boundingBox.maxX >= aabb.minX && boundingBox.minX < aabb.minX)
            {
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesZ && !vert && collidesY && boundingBox.maxZ >= aabb.maxZ && boundingBox.minZ <= aabb.maxZ)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (collidesZ && !vert && collidesY && boundingBox.maxZ >= aabb.minZ && boundingBox.minZ < aabb.minZ)
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (Math.abs(dx) > Math.abs(dz) && dx < 10e2 || dx == 10e3 && dz < 10e2) temp1.z = dz;
            else if (dx < 10e2) temp1.x = dx;
        }
        return collide;
    }

    public static void expandAABBs(final List<AxisAlignedBB> aabbs, final AxisAlignedBB reference)
    {
        final double mx = reference.minX + (reference.maxX - reference.minX) / 2;
        final double my = reference.minY + (reference.maxY - reference.minY) / 2;
        final double mz = reference.minZ + (reference.maxZ - reference.minZ) / 2;

        final int to = 100;

        final int xMax = (int) (mx + to);
        final int xMin = (int) (mx - to);
        final int yMax = (int) (my + to);
        final int yMin = (int) (my - to);
        final int zMax = (int) (mz + to);
        final int zMin = (int) (mz - to);

        double x0, y0, z0, x1, y1, z1;

        for (int i = 0; i < aabbs.size(); i++)
        {
            final AxisAlignedBB box = aabbs.get(i);
            final boolean yMinus = box.minY - to <= reference.minY && reference.minY >= box.minY;
            final boolean yPlus = box.maxY + to >= reference.maxY && reference.maxY <= box.maxY;
            if (yMinus && !yPlus) y0 = yMin;
            else y0 = box.minY;
            if (yPlus && !yMinus) y1 = yMax;
            else y1 = box.maxY;
            final boolean xMinus = box.minX - to <= reference.minX && reference.minX >= box.minX;
            final boolean xPlus = box.maxX + to >= reference.maxX && reference.maxX <= box.maxX;
            if (xMinus && !xPlus) x0 = xMin;
            else x0 = box.minX;
            if (xPlus && !xMinus) x1 = xMax;
            else x1 = box.maxX;
            final boolean zMinus = box.minZ - to <= reference.minZ && reference.minZ >= box.minZ;
            final boolean zPlus = box.maxZ + to >= reference.maxZ && reference.maxZ <= box.maxZ;
            if (zMinus && !zPlus) z0 = zMin;
            else z0 = box.minZ;
            if (zPlus && !zMinus) z1 = zMax;
            else z1 = box.maxZ;
            aabbs.set(i, new AxisAlignedBB(x0, y0, z0, x1, y1, z1));
        }
    }

    public static AxisAlignedBB getAABB(final double minX, final double minY, final double minZ, final double maxX,
            final double maxY, final double maxZ)
    {
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Computes the Determinant of the given matrix, Matrix must be square.
     *
     * @param Matrix
     * @return
     */
    public static double matrixDet(final Matrix3 Matrix)
    {
        double det = 0;
        final int n = Matrix.size;
        if (n == 2) det = Matrix.get(0, 0) * Matrix.get(1, 1) - Matrix.get(1, 0) * Matrix.get(0, 1);
        else for (int i = 0; i < n; i++)
            det += Math.pow(-1, i) * Matrix.get(0, i) * Matrix3.matrixDet(Matrix3.matrixMinor(Matrix, 0, i));
        return det;
    }

    /**
     * Computes the minor matrix formed from removal of the ith row and jth
     * column of matrix.
     *
     * @param Matrix
     * @param i
     * @param j
     * @return
     */
    public static Matrix3 matrixMinor(final Matrix3 input, final int i, final int j)
    {
        final double[][] Matrix = input.toArray();
        final int n = Matrix.length;
        final int m = Matrix[0].length;
        final Double[][] TempMinor = new Double[m - 1][n - 1];
        final List<ArrayList<Double>> row = new ArrayList<>();
        for (int k = 0; k < n; k++)
            if (k != i)
            {
                row.add(new ArrayList<Double>());
                for (int l = 0; l < m; l++)
                    if (l != j) row.get(k - (k > i ? 1 : 0)).add(Matrix[k][l]);
            }
        for (int k = 0; k < n - 1; k++)
            TempMinor[k] = row.get(k).toArray(new Double[0]);
        final Matrix3 Minor = new Matrix3();
        Minor.size = n - 1;
        for (int k = 0; k < n - 1; k++)
            for (int l = 0; l < m - 1; l++)
                Minor.set(k, l, TempMinor[k][l]);
        return Minor;
    }

    /**
     * Transposes the given Matrix
     *
     * @param Matrix
     * @return
     */
    public static Matrix3 matrixTranspose(final Matrix3 Matrix)
    {
        final Matrix3 MatrixT = new Matrix3();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                MatrixT.set(i, j, Matrix.get(j, i));
        return MatrixT;
    }

    /**
     * Merges aabbs together, anything closer than dx, dy or dz are considered
     * same box.
     *
     * @param aabbs
     * @param dx
     * @param dy
     * @param dz
     */
    public static void mergeAABBs(final List<AxisAlignedBB> aabbs, final double dx, final double dy, final double dz)
    {
        final Comparator<AxisAlignedBB> comparator = (arg0, arg1) ->
        {
            final int minX0 = (int) (arg0.minX * 16);
            final int minY0 = (int) (arg0.minY * 16);
            final int minZ0 = (int) (arg0.minZ * 16);
            final int minX1 = (int) (arg1.minX * 16);
            final int minY1 = (int) (arg1.minY * 16);
            final int minZ1 = (int) (arg1.minZ * 16);
            if (minX0 == minX1)
            {
                if (minZ0 == minZ1) return minY0 - minY1;
                return minZ0 - minZ1;
            }
            return minX0 - minX1;
        };
        final AxisAlignedBB[] boxes = aabbs.toArray(new AxisAlignedBB[aabbs.size()]);
        aabbs.clear();
        Arrays.sort(boxes, comparator);
        AxisAlignedBB b1;
        AxisAlignedBB b2;
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                if (Math.abs(b2.maxY - b1.maxY) <= dy && Math.abs(b2.minY - b1.minY) <= dy && Math.abs(b2.maxX
                        - b1.maxX) <= dx && Math.abs(b2.minX - b1.minX) <= dx && Math.abs(b2.minZ - b1.maxZ) <= dz)
                {
                    b1 = b1.union(b2);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                if (Math.abs(b2.maxY - b1.maxY) <= dy && Math.abs(b2.minY - b1.minY) <= dy && Math.abs(b2.maxZ
                        - b1.maxZ) <= dz && Math.abs(b2.minZ - b1.minZ) <= dz && Math.abs(b2.minX - b1.maxX) <= dx)
                {
                    b1 = b1.union(b2);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                if (Math.abs(b2.maxX - b1.maxX) <= dx && Math.abs(b2.minX - b1.minX) <= dx && Math.abs(b2.maxZ
                        - b1.maxZ) <= dz && Math.abs(b2.minZ - b1.minZ) <= dz && Math.abs(b2.minY - b1.maxY) <= dy)
                {
                    b1 = b1.union(b2);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }

        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                // Check if subbox after previous passes, if so, combine.
                if (b2.maxX <= b1.maxX && b2.maxY <= b1.maxY && b2.maxZ <= b1.maxZ && b2.minX >= b1.minX
                        && b2.minY >= b1.minY && b2.minZ >= b1.minZ)
                {
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        for (final AxisAlignedBB b : boxes)
            if (b != null) aabbs.add(b);
    }

    static List<Vector3> toMesh(final ArrayList<Matrix3> boxes)
    {
        final List<Vector3> ret = new ArrayList<>();
        for (final Matrix3 box : boxes)
        {
            final Vector3 vc = box.boxCentre();
            for (final Vector3 v : box.corners(vc))
            {
                boolean has = false;
                for (final Vector3 v1 : ret)
                    if (v1.equals(v))
                    {
                        has = true;
                        break;
                    }
                if (!has) ret.add(v);
            }
        }
        return ret;
    }

    public Vector3[] rows = new Vector3[3];

    int size = 3;

    Vector3[] pointSet;

    public Matrix3()
    {
        this.rows[0] = Vector3.getNewVector();
        this.rows[1] = Vector3.getNewVector();
        this.rows[2] = Vector3.getNewVector();
    }

    public Matrix3(final double d, final double e, final double f)
    {
        this();
        this.rows[1].set(d, e, f);
    }

    public Matrix3(final double[] a, final double[] b, final double[] c)
    {
        this();
        this.rows[0].set(a[0], a[1], a[2]);
        this.rows[1].set(b[0], b[1], b[2]);
        this.rows[2].set(c[0], c[1], c[2]);
    }

    public Matrix3(final Vector3 a, final Vector3 b)
    {
        this(a, b, Vector3.empty);
    }

    public Matrix3(final Vector3 a, final Vector3 b, final Vector3 c)
    {
        this.rows[0] = a.copy();
        this.rows[1] = b.copy();
        this.rows[2] = c.copy();
    }

    public Matrix3 addOffsetTo(final Vector3 pushOffset)
    {
        this.rows[0].addTo(pushOffset);
        this.rows[1].addTo(pushOffset);
        return this;
    }

    public Vector3 boxCentre()
    {
        final Vector3 mid = Vector3.getNewVector();
        final Vector3 temp1 = this.boxMax().copy();
        final Vector3 temp2 = this.boxMax().copy();
        mid.set(temp2.subtractFrom(temp1.subtractFrom(this.boxMin()).scalarMultBy(0.5)));
        return mid;
    }

    public Vector3 boxMax()
    {
        return this.rows[1];
    }

    public Vector3 boxMin()
    {
        return this.rows[0];
    }

    public Vector3 boxRotation()
    {
        return this.rows[2];
    }

    public Matrix3 clear()
    {
        this.rows[0].clear();
        this.rows[1].clear();
        this.rows[2].clear();
        return this;
    }

    public Matrix3 copy()
    {
        final Matrix3 ret = new Matrix3();
        ret.rows[0].set(this.rows[0]);
        ret.rows[1].set(this.rows[1]);
        ret.rows[2].set(this.rows[2]);
        return ret;
    }

    public List<Vector3> corners(final boolean rotate)
    {
        // if (corners.isEmpty())
        final List<Vector3> corners = new ArrayList<>();

        for (int i = 0; i < 8; i++)
            corners.add(Vector3.getNewVector());

        corners.get(0).set(this.boxMin());
        corners.get(1).set(this.boxMax());

        corners.get(2).set(this.boxMin().x, this.boxMin().y, this.boxMax().z);
        corners.get(3).set(this.boxMin().x, this.boxMax().y, this.boxMin().z);
        corners.get(4).set(this.boxMax().x, this.boxMin().y, this.boxMin().z);

        corners.get(5).set(this.boxMin().x, this.boxMax().y, this.boxMax().z);
        corners.get(6).set(this.boxMax().x, this.boxMin().y, this.boxMax().z);
        corners.get(7).set(this.boxMax().x, this.boxMax().y, this.boxMin().z);
        Vector3 mid;
        if (rotate && !this.boxRotation().isEmpty()) mid = this.boxCentre();
        else mid = null;
        if (!this.boxRotation().isEmpty() && mid != null)
        {
            final Vector3 temp = Vector3.getNewVector();
            final Vector3 temp2 = Vector3.getNewVector();
            for (int i = 0; i < 8; i++)
            {
                corners.get(i).subtractFrom(mid);
                temp2.clear();
                temp.clear();
                corners.get(i).set(corners.get(i).rotateAboutAngles(this.boxRotation().y, this.boxRotation().z, temp2,
                        temp));
                corners.get(i).addTo(mid);
            }
        }

        return corners;
    }

    /**
     * 0 = min, min, min; 1 = max, max, max; 2 = min, min, max; 3 = min, max,
     * min; 4 = max, min, min; 5 = min, max, max; 6 = max, min, max; 7 = max.
     * max, min;
     *
     * @return
     */
    public Vector3[] corners(final Vector3 mid)
    {
        return this.corners(mid != null).toArray(new Vector3[8]);
    }

    private List<Vector3> diff(final List<Vector3> cornersA, final List<Vector3> cornersB)
    {
        final ArrayList<Vector3> ret = new ArrayList<>();
        final Vector3 c = Vector3.getNewVector();
        if (this.pointSet == null) this.pointSet = new Vector3[100];

        // Vector3[] pointSet = new Vector3[cornersA.size() * cornersB.size()];

        int n = 0;
        for (final Vector3 a : cornersA)
            for (final Vector3 b : cornersB)
            {
                c.set(a).subtractFrom(b);
                this.pointSet[n++] = c.copy();
            }
        for (int i = 0; i < n; i++)
        {
            final Vector3 v = this.pointSet[i];
            ret.add(v);
            this.pointSet[i] = null;
        }
        // ret.addAll(pointSet);
        return ret;
    }

    public boolean doCollision(final Vector3 boxVelocity, final Entity e)
    {
        if (e == null) return false;
        final Vector3 ent = Vector3.getNewVector();
        ent.set(e);
        this.corners(true);
        final Matrix3 box = new Matrix3();
        box.set(e.getBoundingBox());
        final boolean hit = box.intersects(this);
        return hit;
    }

    public Vector3 get(final int i)
    {
        assert i < 3;
        return this.rows[i];
    }

    public double get(final int i, final int j)
    {
        assert i < 3;
        return this.rows[i].get(j);
    }

    public AxisAlignedBB getBoundingBox()
    {
        final Vector3 v1 = this.boxCentre();
        final Vector3[] corners = this.corners(v1);

        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (final Vector3 v : corners)
        {
            if (v.x > maxX) maxX = v.x;
            if (v.y > maxY) maxY = v.y;
            if (v.z > maxZ) maxZ = v.z;
            if (v.x < minX) minX = v.x;
            if (v.y < minY) minY = v.y;
            if (v.z < minZ) minZ = v.z;
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Matrix3 getOctant(final int octant)
    {
        final Matrix3 ret = this.copy();
        switch (octant)
        {
        case 0:
            ret.rows[0].x = this.rows[0].x + this.rows[1].x / 2;
            ret.rows[0].y = this.rows[0].y + this.rows[1].y / 2;
            ret.rows[0].z = this.rows[0].z + this.rows[1].z / 2;
            return ret;
        case 1:
            ret.rows[1].x = this.rows[1].x - this.rows[1].x / 2;
            ret.rows[0].y = this.rows[0].y + this.rows[1].y / 2;
            ret.rows[0].z = this.rows[0].z + this.rows[1].z / 2;
            return ret;
        case 2:
            ret.rows[1].x = this.rows[1].x - this.rows[1].x / 2;
            ret.rows[1].y = this.rows[1].y - this.rows[1].y / 2;
            ret.rows[0].z = this.rows[0].z + this.rows[1].z / 2;
            return ret;
        case 3:
            ret.rows[0].x = this.rows[0].x + this.rows[1].x / 2;
            ret.rows[1].y = this.rows[1].y - this.rows[1].y / 2;
            ret.rows[0].z = this.rows[0].z + this.rows[1].z / 2;
            return ret;
        case 4:
            ret.rows[0].x = this.rows[0].x + this.rows[1].x / 2;
            ret.rows[0].y = this.rows[0].y + this.rows[1].y / 2;
            ret.rows[1].z = this.rows[1].z - this.rows[1].z / 2;
            return ret;
        case 5:
            ret.rows[1].x = this.rows[1].x - this.rows[1].x / 2;
            ret.rows[0].y = this.rows[0].y + this.rows[1].y / 2;
            ret.rows[1].z = this.rows[1].z - this.rows[1].z / 2;
            return ret;
        case 6:
            ret.rows[1].x = this.rows[1].x - this.rows[1].x / 2;
            ret.rows[1].y = this.rows[1].y - this.rows[1].y / 2;
            ret.rows[1].z = this.rows[1].z - this.rows[1].z / 2;
            return ret;
        case 7:
            ret.rows[0].x = this.rows[0].x + this.rows[1].x / 2;
            ret.rows[1].y = this.rows[1].y - this.rows[1].y / 2;
            ret.rows[1].z = this.rows[1].z - this.rows[1].z / 2;
            return ret;
        }
        return ret;
    }

    public boolean intersects(final List<Vector3> mesh)
    {
        final List<Vector3> cornersA = new ArrayList<>();
        final Vector3 v1 = this.boxCentre();
        for (final Vector3 v : this.corners(v1))
            cornersA.add(v);
        final List<Vector3> diffs = this.diff(cornersA, mesh);
        final boolean temp = Matrix3.containsOrigin(diffs);
        return temp;

    }

    public boolean intersects(final Matrix3 b)
    {
        final List<Vector3> cornersB = new ArrayList<>();
        final Vector3 v1 = this.boxCentre();
        for (final Vector3 v : b.corners(v1))
            cornersB.add(v);
        return this.intersects(cornersB);
    }

    public boolean isInMaterial(final IBlockReader world, final Vector3 location, final Vector3 offset,
            final Material m)
    {
        boolean ret = false;
        final Vector3 ent = location;
        final Vector3[] corners = this.corners(this.boxCentre());
        final Vector3 temp = Vector3.getNewVector();
        final Vector3 dir = Vector3.getNewVector();
        for (int i = 0; i < 8; i++)
        {
            final Vector3 v = corners[i];
            dir.set(v);
            temp.set(dir.addTo(ent).addTo(offset));
            if (temp.getBlockMaterial(world) == m)
            {
                ret = true;
                break;
            }
            if (i % 2 == 0)
            {
                temp.addTo(0, 0.01, 0);
                if (temp.getBlockMaterial(world) == m)
                {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public Matrix3 resizeBox(final double x, final double y, final double z)
    {
        final Matrix3 ret = this.copy();

        ret.boxMin().x -= x;
        ret.boxMin().y -= y;
        ret.boxMin().z -= z;

        ret.boxMax().x += x;
        ret.boxMax().y += y;
        ret.boxMax().z += z;

        return ret;
    }

    public void set(final AxisAlignedBB aabb)
    {
        this.rows[0].x = aabb.minX;
        this.rows[0].y = aabb.minY;
        this.rows[0].z = aabb.minZ;
        this.rows[1].x = aabb.maxX;
        this.rows[1].y = aabb.maxY;
        this.rows[1].z = aabb.maxZ;
        this.rows[2].clear();
    }

    public void set(final int i, final int j, final double k)
    {
        this.rows[i].set(j, k);
    }

    public Matrix3 set(final int i, final Vector3 j)
    {
        assert i < 3;
        this.rows[i] = j;
        return this;
    }

    public void set(final Matrix3 box)
    {
        this.rows[0].set(box.rows[0]);
        this.rows[1].set(box.rows[1]);
        this.rows[2].set(box.rows[2]);
    }

    public Vector3[][] splitBox()
    {
        final Vector3 v1 = this.boxCentre();
        final Vector3[] corners = this.corners(v1);
        double dx = this.boxMax().x - this.boxMin().x;
        double dz = this.boxMax().z - this.boxMin().z;
        dx = Math.abs(dx);
        dz = Math.abs(dz);
        if (dx <= 1 && dz <= 1 || dx < 0.1 || dz < 0.1) return new Vector3[][] { corners };

        dx = Math.max(dx, 1);

        if (dz > 2 * dx)
        {
            final int num = (int) (dz / dx);
            final Vector3[][] ret = new Vector3[num][8];

            final Vector3 min1 = corners[0];
            final Vector3 max1 = corners[2];
            final Vector3 dir1 = max1.subtract(min1).scalarMultBy(1d / num);

            final Vector3 min2 = corners[3];
            final Vector3 max2 = corners[5];
            final Vector3 dir2 = max2.subtract(min2).scalarMultBy(1d / num);

            final Vector3 min3 = corners[7];
            final Vector3 max3 = corners[1];
            final Vector3 dir3 = max3.subtract(min3).scalarMultBy(1d / num);

            final Vector3 min4 = corners[4];
            final Vector3 max4 = corners[6];
            final Vector3 dir4 = max4.subtract(min4).scalarMultBy(1d / num);

            for (int i = 0; i < num; i++)
            {
                ret[i][0] = dir1.scalarMult(i).addTo(min1);
                ret[i][1] = dir2.scalarMult(i).addTo(min2);
                ret[i][2] = dir3.scalarMult(i).addTo(min3);
                ret[i][3] = dir4.scalarMult(i).addTo(min4);

                ret[i][4] = dir1.scalarMult(i + 1).addTo(min1);
                ret[i][5] = dir2.scalarMult(i + 1).addTo(min2);
                ret[i][6] = dir3.scalarMult(i + 1).addTo(min3);
                ret[i][7] = dir4.scalarMult(i + 1).addTo(min4);
            }
            return ret;
        }
        return new Vector3[][] { corners };
    }

    public double[][] toArray()
    {
        return new double[][] { { this.rows[0].x, this.rows[0].y, this.rows[0].z }, { this.rows[1].x, this.rows[1].y,
                this.rows[1].z }, { this.rows[2].x, this.rows[2].y, this.rows[2].z } };
    }

    @Override
    public String toString()
    {
        final String eol = System.getProperty("line.separator");
        return eol + "0: " + this.rows[0].toString() + eol + "1: " + this.rows[1].toString() + eol + "2 : "
                + this.rows[2].toString();
    }

}
