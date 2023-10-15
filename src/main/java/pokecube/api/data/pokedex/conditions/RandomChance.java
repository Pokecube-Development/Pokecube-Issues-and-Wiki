package pokecube.api.data.pokedex.conditions;

import java.util.Random;

import pokecube.api.entity.pokemob.IPokemob;

public class RandomChance implements PokemobCondition
{
    public double chance;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return new Random(mobIn.getRNGValue()).nextDouble() > chance;
    }

    @Override
    public void init()
    {
        if (chance > 1) chance /= 100;
    }
}