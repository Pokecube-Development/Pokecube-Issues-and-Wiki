package pokecube.api.data.abilities.json.pokemob.predicates;

import pokecube.api.data.abilities.json.common.BasePredicate;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;

public class IsType extends BasePredicate<IPokemob>
{
    String type;
    boolean reversed = false;
    PokeType _type = null;

    @Override
    public void init()
    {
        _type = PokeType.getType(type);
    }

    @Override
    public boolean test(IPokemob t)
    {
        boolean check = t.isType(_type);
        return reversed ? !check : check;
    }
}
