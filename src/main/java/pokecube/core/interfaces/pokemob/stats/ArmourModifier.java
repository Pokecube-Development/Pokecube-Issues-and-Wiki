package pokecube.core.interfaces.pokemob.stats;

import pokecube.core.interfaces.IPokemob.Stats;

public class ArmourModifier implements IStatsModifiers
{
    public float[] values = new float[Stats.values().length];

    public ArmourModifier()
    {
    }

    @Override
    public float getModifier(final Stats stat)
    {
        return this.values[stat.ordinal()];
    }

    @Override
    public float getModifierRaw(final Stats stat)
    {
        return this.values[stat.ordinal()];
    }

    @Override
    public int getPriority()
    {
        return 200000;
    }

    @Override
    public float apply(final Stats stat, final float valueIn)
    {
        return valueIn + this.getModifier(stat);
    }

    @Override
    public boolean persistant()
    {
        return true;
    }

    @Override
    public void setModifier(final Stats stat, final float value)
    {
        this.values[stat.ordinal()] = value;
    }
}
