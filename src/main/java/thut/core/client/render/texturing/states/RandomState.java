package thut.core.client.render.texturing.states;

import java.util.Random;

import thut.api.entity.IMobTexturable;

public class RandomState
{
    Random rng = new Random();

    public double chance = 0.005;
    public double[] arr;
    public int duration = 1;

    public RandomState(final String trigger, final double[] arr)
    {
        this.arr = arr;
        final String[] args = trigger.split(":");
        if (args.length > 1) this.chance = Double.parseDouble(args[1]);
        if (args.length > 2) this.duration = Integer.parseInt(args[2]);
    }

    public boolean apply(final double[] toFill, final IMobTexturable mob)
    {
        // Set the seed
        rng.setSeed(mob.getRandomSeed() ^ mob.getEntity().tickCount);
        // Prime the RNG
        rng.nextFloat();

        final double[] arr = this.arr;
        if (rng.nextFloat() < this.chance)
        {
            toFill[0] = arr[0];
            toFill[1] = arr[1];
            return true;
        }
        return false;
    }
}
