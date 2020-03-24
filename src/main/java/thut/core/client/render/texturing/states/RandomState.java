package thut.core.client.render.texturing.states;

public class RandomState
{

    public double   chance   = 0.005;
    public double[] arr;
    public int      duration = 1;

    public  RandomState(final String trigger, final double[] arr)
    {
        this.arr = arr;
        final String[] args = trigger.split(":");
        if (args.length > 1) this.chance = Double.parseDouble(args[1]);
        if (args.length > 2) this.duration = Integer.parseInt(args[2]);
    }

}
