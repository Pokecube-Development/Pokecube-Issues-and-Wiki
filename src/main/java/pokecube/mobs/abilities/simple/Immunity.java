package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;

public class Immunity extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final IPokemob attacker = move.attacker;
        if (attacker == mob || !move.pre || attacker == move.attacked) return;
        if ((move.statusChange & IMoveConstants.STATUS_PSN) != 0) move.statusChange = IMoveConstants.STATUS_NON;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        if ((mob.getStatus() & IMoveConstants.STATUS_PSN) != 0) mob.healStatus();
    }

}
