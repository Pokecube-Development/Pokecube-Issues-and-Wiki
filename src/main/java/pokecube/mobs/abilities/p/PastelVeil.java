package pokecube.mobs.abilities.p;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

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
