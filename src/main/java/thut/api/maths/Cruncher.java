package thut.api.maths;

public class Cruncher
{

    public static double cubeRoot(double num)
    {
        return Math.pow(num, 1 / 3d);
    }

    public static void fillFromInt(int[] toFill, int vec)
    {
        toFill[0] = (vec & 1023) - 512;
        toFill[1] = (vec >> 10 & 1023) - 512;
        toFill[2] = (vec >> 20 & 1023) - 512;
    }

    public static int[] getInts(int index)
    {
        final int[] ret = new int[4];
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
        ret[0] = cr;
        ret[1] = si;
        ret[2] = rsd;
        ret[3] = rcd;
        return ret;
    }

    public static int getVectorInt(int x, int y, int z)
    {
        final int i = x + 512;
        final int j = y + 512;
        final int k = z + 512;
        return i + (j << 10) + (k << 20);
    }

    public static int getVectorInt(Vector3 rHat)
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

    public static long getVectorLong(Vector3 rHat)
    {
        if (rHat.magSq() > 1000000) new Exception().printStackTrace();
        final int i = rHat.intX() + 0xFFF;
        final int j = rHat.intY() + 0xFFF;
        final int k = rHat.intZ() + 0xFFF;
        return i + (j << 12) + (k << 24);
    }

    public static void indexToVals(int radius, int index, int diffSq, int diffCb, Vector3 toFill)
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

    public static void indexToVals(int index, Vector3 toFill)
    {
        if (index > 0)
        {
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

    public Double[] set1 = new Double[] { 123456d };
    public Object[] set6 = new Object[] { null };
    public int      n;

    double temp  = 0.0D;
    Object temp6 = null;

    public Cruncher()
    {
    }

    private void exchange(int i, int j)
    {
        if (this.set1[0] != 123456d || this.set1.length == this.n)
        {
            this.temp = this.set1[i].doubleValue();
            this.set1[i] = this.set1[j];
            this.set1[j] = Double.valueOf(this.temp);
        }
        if (this.set6[0] != null || this.set6.length == this.n)
        {
            this.temp6 = this.set6[i];
            this.set6[i] = this.set6[j];
            this.set6[j] = this.temp6;
        }
    }

    private void quicksort(int low, int high)
    {
        int i = low;
        int j = high;
        final double pivot = this.set1[low + (high - low) / 2].doubleValue();
        while (i <= j)
        {
            while (this.set1[i].doubleValue() < pivot)
                i++;
            while (this.set1[j].doubleValue() > pivot)
                j--;
            if (i <= j)
            {
                this.exchange(i, j);
                i++;
                j--;
            }
        }
        if (low < j) this.quicksort(low, j);
        if (i < high) this.quicksort(i, high);
    }

    public void sort(Double[] vals1, Object[] vals2)
    {
        if (vals1 == null || vals1.length == 0) return;
        this.set1 = vals1;
        this.set6 = vals2;
        this.n = this.set1.length;

        this.quicksort(0, this.n - 1);
    }
}
