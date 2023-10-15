package pokecube.api.data.pokedex.conditions;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.Tools;

public class HasMove implements PokemobCondition
{
    public String move;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return Tools.hasMove(this.move, mobIn);
    }
}