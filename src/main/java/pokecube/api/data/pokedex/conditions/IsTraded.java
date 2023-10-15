package pokecube.api.data.pokedex.conditions;

import pokecube.api.entity.pokemob.IPokemob;

public class IsTraded implements PokemobCondition
{
    @Override
    public boolean matches(IPokemob mobIn)
    {
        return mobIn.traded();
    }
}