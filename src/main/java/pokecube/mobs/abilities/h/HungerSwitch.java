package pokecube.mobs.abilities.h;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;

public class HungerSwitch extends Ability
{
	private static PokedexEntry base;
    private static PokedexEntry hunger;
    private static boolean      noTurn = false;
    
    @Override
    public void onUpdate(IPokemob mob)
    {
        if (HungerSwitch.noTurn) return;
        if (HungerSwitch.base == null)
        {
        	HungerSwitch.base = Database.getEntry("Morpeko");
        	HungerSwitch.hunger = Database.getEntry("Morpeko Hangry");
        	HungerSwitch.noTurn = HungerSwitch.base == null || HungerSwitch.hunger == null;
            if (HungerSwitch.noTurn) return;
        }
        final PokedexEntry mobs = mob.getPokedexEntry();
        if (!(mobs == HungerSwitch.base || mobs == HungerSwitch.hunger)) return;

        if (mob.getHungerTime() > 20)
        {
            if (mobs == HungerSwitch.base) mob.setPokedexEntry(HungerSwitch.hunger);
        }
        else if (mobs == HungerSwitch.hunger) mob.setPokedexEntry(HungerSwitch.base);
    }
}
