package pokecube.api.data.pokedex.conditions;

import pokecube.api.entity.pokemob.IPokemob;

public class AtLeastLevel implements PokemobCondition
{
    public int level;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return mobIn.getLevel() >= level;
    }

}
