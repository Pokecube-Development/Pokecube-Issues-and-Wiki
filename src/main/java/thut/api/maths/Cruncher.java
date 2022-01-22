package thut.api.maths;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Mth;

public class Cruncher
{
    private static short[][] CUBECACHE;
    private static short[][] SPHERECACHE;

    public static int[][] RADII = new int[1024][2];

    public static int maxSphereR = 0;

    public static boolean useCache = false;

    public static void init()
    {
        // already initialized!
        if (Cruncher.useCache) return;
        Cruncher.CUBECACHE = new short[128 * 128 * 128][];
        final Vector3 temp = new Vector3();
        for (int i = 0; i < Cruncher.CUBECACHE.length; i++)
        {
            final short[] var = new short[3];
            Cruncher.indexToVals(i, temp, true);
            var[0] = (short) temp.intX();
            var[1] = (short) temp.intY();
            var[2] = (short) temp.intZ();
            Cruncher.CUBECACHE[i] = var;
        }
        Cruncher.SPHERECACHE = new short[256 * 256 * 256][];
        final IntSet added = new IntOpenHashSet(256 * 256 * 256);
        double radius = 0.25;
        final double C = 3.809;
        double area = 4 * Math.PI * radius * radius;
        final float grid = 0.5f;
        float N = (float) Math.ceil(area / grid);
        int n = 0;
        for (; n < Cruncher.SPHERECACHE.length;)
        {
            final int[] rads = Cruncher.RADII[(int) radius];
            if (rads[0] == 0 && n != 0) rads[0] = n;
            int k;
            float phi_k_1 = 0;
            // Spiral algorithm based on https://doi.org/10.1007/BF03024331
            for (k = 1; k <= N; k++)
            {
                final float h_k = -1 + 2 * (k - 1) / (N - 1);
                final float sin_theta = (float) Math.sqrt(1 - h_k * h_k);
                final float phi_k = (float) (k > 1 && k < N ? (phi_k_1 + C / Math.sqrt(N * (1 - h_k * h_k))) % (2
                        * Math.PI) : 0);
                phi_k_1 = phi_k;
                final double x = sin_theta * Mth.cos(phi_k) * radius;
                final double y = h_k * radius;
                final double z = sin_theta * Mth.sin(phi_k) * radius;
                temp.set(x, y, z);
                final int index = Cruncher.getVectorInt(temp);
                if (!added.add(index)) continue;
                temp.set(x, y, z);
                final short[] var = new short[3];
                Cruncher.SPHERECACHE[n++] = var;
                var[0] = (short) temp.intX();
                var[1] = (short) temp.intY();
                var[2] = (short) temp.intZ();
                Cruncher.maxSphereR = (int) radius;
                if (n >= Cruncher.SPHERECACHE.length) break;
            }
            rads[1] = n - 1;
            radius += grid;
            area = 4 * Math.PI * radius * radius;
            N = (float) Math.ceil(area / grid);
        }
        Cruncher.useCache = true;
    }

    public static double cubeRoot(final double num)
    {
        return Math.pow(num, 1 / 3d);
    }

    public static int getVectorInt(final Vector3 rHat)
    {
        if (rHat.magSq() > 511 * 511)
        {
            new Exception().printStackTrace();
            return 0;
        }
        final int i = rHat.intX() + 512;
        final int j = rHat.intY() + 512;
        final int k = rHat.intZ() + 512;
        return i + (j << 10) + (k << 20);
    }

    public static long getVectorLong(final Vector3 rHat)
    {
        return rHat.getPos().asLong();
    }

    public static void indexToVals(final int radius, final int index, final int diffSq, final int diffCb,
            final Vector3 toFill)
    {
        toFill.x = 0;
        toFill.y = 0;
        toFill.z = 0;
        final int layerSize = (2 * radius + 1) * (2 * radius + 1);
        if (index == 0)
        {
            toFill.x = -radius;
            toFill.y = radius;
            toFill.z = -radius;
            return;
        }
        // Fill y
        {
            if (index >= layerSize && index < diffCb - layerSize)
            {
                int temp = (index - layerSize) / diffSq + 1;
                temp -= radius;
                temp = temp > radius ? radius : temp < -radius ? -radius : temp;
                toFill.y = temp;
            }
            else if (index > layerSize) toFill.y = -radius;
            else toFill.y = radius;
        }
        // Fill x
        if (!(toFill.y == radius || toFill.y == -radius))
        {
            int temp = index % diffSq;
            if (temp < diffSq / 2)
            {
                if (temp < radius) toFill.x = temp;
                else if (temp > diffSq / 2 - radius) toFill.x = -(temp - diffSq / 2);
                else toFill.x = radius;
            }
            else if (temp > diffSq / 2)
            {
                temp -= diffSq / 2;
                if (temp < radius) toFill.x = -temp;
                else if (temp > diffSq / 2 - radius) toFill.x = temp - diffSq / 2;
                else toFill.x = -radius;
            }
        }
        else
        {
            int temp = index % layerSize;
            temp = temp % (2 * radius + 1);
            temp -= radius;
            toFill.x = temp;
        }
        // Fill z
        if (!(toFill.y == radius || toFill.y == -radius))
        {
            int temp = index % diffSq;
            temp = (temp + 2 * radius - 1) % diffSq + 1;
            if (temp < diffSq / 2)
            {
                if (temp < radius) toFill.z = temp;
                else if (temp > diffSq / 2 - radius) toFill.z = -(temp - diffSq / 2);
                else toFill.z = radius;
            }
            else if (temp > diffSq / 2)
            {
                temp -= diffSq / 2;
                if (temp < radius) toFill.z = -temp;
                else if (temp > diffSq / 2 - radius) toFill.z = temp - diffSq / 2;
                else toFill.z = -radius;
            }
        }
        else
        {
            int temp = index % layerSize / (2 * radius + 1);
            temp -= radius;
            toFill.z = temp;
        }
    }

    public static void indexToVals(final int index, final Vector3 toFill)
    {
        if (index > 0)
        {
            if (Cruncher.useCache && index < Cruncher.CUBECACHE.length)
            {
                toFill.set(Cruncher.CUBECACHE[index]);
                return;
            }
            int cr, rsd, rcd;
            cr = (int) Math.floor(Cruncher.cubeRoot(index));
            cr = (cr - 1) / 2 + 1;
            int temp = 2 * cr - 1;
            int crc = temp * temp * temp;
            if (crc > 2) crc += 1;
            final int si = index - crc;
            final int crs = temp * temp;
            temp = 2 * (cr + 1) - 1;
            final int nrs = temp * temp;
            rsd = nrs - crs;
            rcd = 24 * cr * cr + 2;
            Cruncher.indexToVals(cr, si, rsd, rcd, toFill);
        }
        else toFill.x = toFill.y = toFill.z = 0;
    }

    public static void indexToVals(final int index, final Vector3 toFill, final boolean cube)
    {
        if (cube || !Cruncher.useCache || index >= Cruncher.SPHERECACHE.length) Cruncher.indexToVals(index, toFill);
        else if (index < Cruncher.SPHERECACHE.length) toFill.set(Cruncher.SPHERECACHE[index]);
    }
}
