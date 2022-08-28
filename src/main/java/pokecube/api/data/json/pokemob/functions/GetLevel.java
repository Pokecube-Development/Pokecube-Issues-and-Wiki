package pokecube.api.data.json.pokemob.functions;

import pokecube.api.data.json.common.BaseFunction;
import pokecube.api.entity.pokemob.IPokemob;

public class GetLevel extends BaseFunction<IPokemob, Integer>
{
    @Override
    public Integer apply(IPokemob t)
    {
        return t.getLevel();
    }
}
