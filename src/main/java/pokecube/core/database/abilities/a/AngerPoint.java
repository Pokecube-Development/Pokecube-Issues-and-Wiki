package pokecube.core.database.abilities.a;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;

public class AngerPoint extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (move.didCrit && mob == move.attacked) MovesUtils.handleStats2(mob, move.attacker.getEntity(),
                IMoveConstants.ATTACK, IMoveConstants.RAISE);
    }
}
