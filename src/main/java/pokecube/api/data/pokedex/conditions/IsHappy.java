package pokecube.api.data.pokedex.conditions;

import pokecube.api.entity.pokemob.IPokemob;

public class IsHappy implements PokemobCondition
{
    boolean above = true;
    int amount = 220;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return above ? mobIn.getHappiness() >= amount : mobIn.getHappiness() <= amount;
    }

}
