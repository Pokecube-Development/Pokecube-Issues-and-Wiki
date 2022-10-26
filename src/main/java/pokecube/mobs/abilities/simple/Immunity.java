package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "immunity")
public class Immunity extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if ((move.status_effects & IMoveConstants.STATUS_PSN2) == IMoveConstants.STATUS_PSN2)
            move.status_effects -= IMoveConstants.STATUS_PSN2;
        else if ((move.status_effects & IMoveConstants.STATUS_PSN) > 0)
            move.status_effects -= IMoveConstants.STATUS_PSN;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        if ((mob.getStatus() & IMoveConstants.STATUS_PSN) != 0) mob.healStatus();
    }

}
