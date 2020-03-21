package thut.core.client.render.texturing.states;

import java.util.Random;

import thut.api.entity.IMobTexturable;

public class RandomFixed
{

    public int    seedModifier = 0;
    public double rangeU       = 1;
    public double rangeV       = 1;
    public double startU       = 0;
    public double startV       = 0;

    Random rand = new Random();

    public RandomFixed()
    {
    }

    public boolean applyState(final double[] toFill, final IMobTexturable mob)
    {
        double dx = 0;
        double dy = 0;
        final long seed = mob.getRandomSeed() + ((long) this.seedModifier << 32);
        this.rand.setSeed(seed);
        dx = this.startU + this.rand.nextDouble() * this.rangeU;
        dy = this.startV + this.rand.nextDouble() * this.rangeV;
        // dx = 0;
        // dy = 0;
        toFill[0] = dx;
        toFill[1] = dy;
        return true;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null) return false;
        return obj.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode()
    {
        return this.seedModifier;
    }
}
