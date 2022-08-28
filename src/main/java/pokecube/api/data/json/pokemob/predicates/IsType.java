package pokecube.api.data.json.pokemob.predicates;

import pokecube.api.data.json.common.BasePredicate;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;

public class IsType extends BasePredicate<IPokemob>
{
    String type;
    PokeType _type = null;

    @Override
    public void init()
    {
        _type = PokeType.getType(type);
    }

    @Override
    public boolean test(IPokemob t)
    {
        return t.isType(_type);
    }
}
