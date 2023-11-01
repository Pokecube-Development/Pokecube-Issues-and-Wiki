package thut.core.client.render.texturing.states;

public class Sequence
{
    public double[] arr;
    public boolean  shift = true;

    public Sequence(final double[] arr)
    {
        this.arr = arr;
        for (final double d : arr)
            if (d >= 1) this.shift = false;
    }

}
