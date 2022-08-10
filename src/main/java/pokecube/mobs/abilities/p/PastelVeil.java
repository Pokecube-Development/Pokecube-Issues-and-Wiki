package pokecube.mobs.abilities.p;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.database.abilities.Ability;

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
