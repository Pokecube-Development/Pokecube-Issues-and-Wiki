package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveConstants;

public class PastelVeil extends Ability
{
	@Override
    public void onUpdate(final IPokemob mob)
    {
		if(mob.getStatus() == IMoveConstants.STATUS_PSN) {
			mob.healStatus();
		}
    }
}
