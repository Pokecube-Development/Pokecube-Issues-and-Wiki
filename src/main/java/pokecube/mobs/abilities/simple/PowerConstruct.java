package pokecube.mobs.abilities.simple;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;

@AbilityProvider(name = "power-construct")
public class PowerConstruct extends Ability
{
	private static PokedexEntry base10;
    private static PokedexEntry PowerConstruct10;

    private static PokedexEntry base50;
    private static PokedexEntry PowerConstruct50;


    private static boolean      noTurnBase = false;

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (PowerConstruct.noTurnBase) return;
        if (PowerConstruct.base10 == null)
        {
        	PowerConstruct.base10 = Database.getEntry("zygarde-10");
        	PowerConstruct.PowerConstruct10 = Database.getEntry("zygarde-complete");
        	PowerConstruct.base50 = Database.getEntry("zygarde-50");
        	PowerConstruct.PowerConstruct50 = Database.getEntry("zygarde-complete");
        	PowerConstruct.noTurnBase = PowerConstruct.base10 == null || PowerConstruct.PowerConstruct10 == null;
            if (PowerConstruct.noTurnBase) return;
        }

        final PokedexEntry mobs = mob.getPokedexEntry();
        if ((mobs == PowerConstruct.base10 || mobs == PowerConstruct.PowerConstruct10))
        {
	        if (mob.getEntity().getHealth() < (mob.getEntity().getMaxHealth() / 2))
	        {
	            if (mobs == PowerConstruct.base10) mob.setPokedexEntry(PowerConstruct.PowerConstruct10);
	        }
        else if (mobs == PowerConstruct.PowerConstruct10) mob.setPokedexEntry(PowerConstruct.base10);
        }

        if ((mobs == PowerConstruct.base50 || mobs == PowerConstruct.PowerConstruct50))
        {
	        if (mob.getEntity().getHealth() < (mob.getEntity().getMaxHealth() / 2))
	        {
	            if (mobs == PowerConstruct.base50) mob.setPokedexEntry(PowerConstruct.PowerConstruct50);
	        }
        else if (mobs == PowerConstruct.PowerConstruct50) mob.setPokedexEntry(PowerConstruct.base50);
        }
    }
}
