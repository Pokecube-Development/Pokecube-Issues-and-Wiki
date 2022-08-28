package pokecube.api.data.json.movepacket.predicates;

import pokecube.api.data.json.common.BasePredicate;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class HasFlag extends BasePredicate<MovePacket>
{
    String flag;

    @Override
    public boolean test(MovePacket t)
    {
        return MovePacket.getFlag(flag, t);
    }

}
