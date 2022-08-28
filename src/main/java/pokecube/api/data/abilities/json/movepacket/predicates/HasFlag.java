package pokecube.api.data.abilities.json.movepacket.predicates;

import pokecube.api.data.abilities.json.common.BasePredicate;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class HasFlag extends BasePredicate<MovePacket>
{
    String flag;
    boolean reversed = false;

    @Override
    public boolean test(MovePacket t)
    {
        boolean check = MovePacket.getFlag(flag, t);
        return reversed ? !check : check;
    }

}
