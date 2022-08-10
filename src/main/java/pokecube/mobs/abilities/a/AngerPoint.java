package pokecube.mobs.abilities.a;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.database.abilities.Ability;
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
