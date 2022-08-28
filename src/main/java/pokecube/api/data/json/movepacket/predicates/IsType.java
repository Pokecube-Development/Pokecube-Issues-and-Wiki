package pokecube.api.data.json.movepacket.predicates;

import pokecube.api.data.json.common.BasePredicate;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.utils.PokeType;

public class IsType extends BasePredicate<MovePacket>
{
    String type;
    PokeType _type = null;

    @Override
    public void init()
    {
        _type = PokeType.getType(type);
    }

    @Override
    public boolean test(MovePacket t)
    {
        return t.attackType == _type;
    }
}
