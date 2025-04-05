package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "water-veil")
public class WaterVeil extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if ((move.status_effects & IMoveConstants.STATUS_BRN) > 0) move.status_effects -= IMoveConstants.STATUS_BRN;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getStatus() == IMoveConstants.STATUS_BRN) mob.healStatus();
    }

}
