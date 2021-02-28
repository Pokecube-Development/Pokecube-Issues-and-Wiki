package pokecube.core.interfaces.pokemob.stats;

import pokecube.core.interfaces.IPokemob.Stats;

public class DefaultModifiers implements IStatsModifiers
{
    public float[] values = new float[Stats.values().length];

    public DefaultModifiers()
    {
    }

    @Override
    public float getModifier(final Stats stat)
    {
        return this.modifierToRatio((byte) this.values[stat.ordinal()], stat.ordinal() > 5);
    }

    @Override
    public float getModifierRaw(final Stats stat)
    {
        return this.values[stat.ordinal()];
    }

    @Override
    public int getPriority()
    {
        return 100;
    }

    @Override
    public float apply(final Stats stat, final float valueIn)
    {
        return valueIn * this.getModifier(stat);
    }

    public float modifierToRatio(final byte mod, final boolean accuracy)
    {
        float modifier = 1;
        if (mod == 0) modifier = 1;
        else if (mod == 1) modifier = !accuracy ? 1.5f : 4 / 3f;
        else if (mod == 2) modifier = !accuracy ? 2 : 5 / 3f;
        else if (mod == 3) modifier = !accuracy ? 2.5f : 2;
        else if (mod == 4) modifier = !accuracy ? 3 : 7 / 3f;
        else if (mod == 5) modifier = !accuracy ? 3.5f : 8 / 3f;
        else if (mod == 6) modifier = !accuracy ? 4 : 3;
        else if (mod == -1) modifier = !accuracy ? 2 / 3f : 3 / 4f;
        else if (mod == -2) modifier = !accuracy ? 1 / 2f : 3 / 5f;
        else if (mod == -3) modifier = !accuracy ? 2 / 5f : 3 / 6f;
        else if (mod == -4) modifier = !accuracy ? 1 / 3f : 3 / 7f;
        else if (mod == -5) modifier = !accuracy ? 2 / 7f : 3 / 8f;
        else if (mod == -6) modifier = !accuracy ? 1 / 4f : 3 / 9f;
        return modifier;
    }

    @Override
    public boolean persistant()
    {
        return false;
    }

    @Override
    public void setModifier(final Stats stat, final float value)
    {
        this.values[stat.ordinal()] = value;
    }

}
