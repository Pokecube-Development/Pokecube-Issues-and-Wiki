package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "insomnia")
public class Insomnia extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if ((move.status_effects & IMoveConstants.STATUS_SLP) > 0) move.status_effects -= IMoveConstants.STATUS_SLP;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getStatus() == IMoveConstants.STATUS_SLP) mob.healStatus();
    }

}
