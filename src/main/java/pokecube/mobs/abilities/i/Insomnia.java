package pokecube.mobs.abilities.i;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.database.abilities.Ability;

public class Insomnia extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final IPokemob attacker = move.attacker;
        if (attacker == mob || !move.pre || attacker == move.attacked) return;
        if (move.statusChange == IMoveConstants.STATUS_SLP) move.statusChange = IMoveConstants.STATUS_NON;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getStatus() == IMoveConstants.STATUS_SLP) mob.healStatus();
    }

}
