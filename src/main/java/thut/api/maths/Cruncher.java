package thut.api.maths;

public class Cruncher
{
    private static final int[][] CACHE = new int[256 * 256 * 256][];

    public static boolean useCache = false;

    public static void init()
    {
        // already initialized!
        if (Cruncher.useCache) return;
        final Vector3 temp = Vector3.getNewVector();
        for (int i = 0; i < Cruncher.CACHE.length; i++)
        {
            final int[] var = new int[3];
            Cruncher.indexToVals(i, temp);
            var[0] = temp.intX();
            var[1] = temp.intY();
            var[2] = temp.intZ();
            Cruncher.CACHE[i] = var;
        }
        Cruncher.useCache = true;
    }

    public static double cubeRoot(final double num)
    {
        return Math.pow(num, 1 / 3d);
    }

    public static int getVectorInt(final Vector3 rHat)
    {
        if (rHat.magSq() > 261121) // 511*511
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
        if (rHat.magSq() > 1000000) new Exception().printStackTrace();
        final int i = rHat.intX() + 0xFFF;
        final int j = rHat.intY() + 0xFFF;
        final int k = rHat.intZ() + 0xFFF;
        return i + (j << 12) + (k << 24);
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
            if (index < Cruncher.CACHE.length && Cruncher.useCache)
            {
                toFill.set(Cruncher.CACHE[index]);
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
}
