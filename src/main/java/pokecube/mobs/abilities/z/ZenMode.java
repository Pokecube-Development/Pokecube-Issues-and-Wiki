package pokecube.mobs.abilities.z;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;

public class ZenMode extends Ability
{
	private static PokedexEntry baseNormal;
    private static PokedexEntry zenmodeNormal;
    
    private static PokedexEntry baseGalar;
    private static PokedexEntry zenmodeGalar;

    
    private static boolean      noTurnBase = false;

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (ZenMode.noTurnBase) return;
        if (ZenMode.baseNormal == null)
        {
        	ZenMode.baseNormal = Database.getEntry("Darmanitan");
        	ZenMode.zenmodeNormal = Database.getEntry("Darmanitan Zen");
        	ZenMode.baseGalar = Database.getEntry("Darmanitan Galar");
        	ZenMode.zenmodeGalar = Database.getEntry("Darmanitan Zen Galar");
        	ZenMode.noTurnBase = ZenMode.baseNormal == null || ZenMode.zenmodeNormal == null; 
            if (ZenMode.noTurnBase) return;
        }
        
        final PokedexEntry mobs = mob.getPokedexEntry();
        if ((mobs == ZenMode.baseNormal || mobs == ZenMode.zenmodeNormal))
        {
	        if (mob.getEntity().getHealth() < (mob.getEntity().getMaxHealth() / 2))
	        {
	            if (mobs == ZenMode.baseNormal) mob.setPokedexEntry(ZenMode.zenmodeNormal);
	        }
        else if (mobs == ZenMode.zenmodeNormal) mob.setPokedexEntry(ZenMode.baseNormal);
        }
        
        if ((mobs == ZenMode.baseGalar || mobs == ZenMode.zenmodeGalar))
        {
	        if (mob.getEntity().getHealth() < (mob.getEntity().getMaxHealth() / 2))
	        {
	            if (mobs == ZenMode.baseGalar) mob.setPokedexEntry(ZenMode.zenmodeGalar);
	        }
        else if (mobs == ZenMode.zenmodeGalar) mob.setPokedexEntry(ZenMode.baseGalar);
        }
    }
}
