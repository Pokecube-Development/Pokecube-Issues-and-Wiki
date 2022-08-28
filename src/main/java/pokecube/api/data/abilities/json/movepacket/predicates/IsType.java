package pokecube.api.data.abilities.json.movepacket.predicates;

import pokecube.api.data.abilities.json.common.BasePredicate;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.utils.PokeType;

public class IsType extends BasePredicate<MovePacket>
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
    public boolean test(MovePacket t)
    {
        return reversed ? t.attackType != _type : t.attackType == _type;
    }
}
