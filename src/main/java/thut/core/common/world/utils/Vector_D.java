package thut.core.common.world.utils;

import thut.api.world.utils.Vector;

public class Vector_D extends Vector_Base<Double>
{
    /**
     * Returns the greatest integer less than or equal to the double
     * argument
     */
    public static int floor(double value)
    {
        final int i = (int) value;
        return value < i ? i - 1 : i;
    }

    @Override
    public void add(Vector<Double> other)
    {
        final Double[] v2 = other.getVector();
        final Double[] v1 = this.array;
        v1[0] += v2[0];
        v1[1] += v2[1];
        v1[2] += v2[2];
    }

    @Override
    public int compareTo(Vector<Double> o)
    {
        // Lets see if this causes any issues?
        return (int) this.dot(o);
    }

    @Override
    void init()
    {
        this.array = new Double[3];
    }

    @Override
    public void subtract(Vector<Double> other)
    {
        final Double[] v2 = other.getVector();
        final Double[] v1 = this.array;
        v1[0] -= v2[0];
        v1[1] -= v2[1];
        v1[2] -= v2[2];
    }

    public void toInts(Vector_I toSet)
    {
        final Integer[] v2 = toSet.getVector();
        final Double[] v1 = this.array;
        v2[0] = Vector_D.floor(v1[0]);
        v2[1] = Vector_D.floor(v1[1]);
        v2[2] = Vector_D.floor(v1[2]);
    }

}
