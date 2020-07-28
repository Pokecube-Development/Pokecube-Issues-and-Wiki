package pokecube.mobs.abilities.i;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.moves.MoveEntry.Category;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class IceFace extends Ability
{
	private static PokedexEntry Ice;
    private static PokedexEntry noIce;
    
    private static boolean      noTurnBase = false;

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (IceFace.noTurnBase) return;
        if (IceFace.Ice == null)
        {
        	IceFace.Ice = Database.getEntry("Eiscue");
        	IceFace.noIce = Database.getEntry("Eiscue Noice");
        	IceFace.noTurnBase = IceFace.Ice == null || IceFace.noIce == null; 
            if (IceFace.noTurnBase) return;
        }
        
        final Move_Base attack = move.getMove();
        final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects effects = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
        final PokedexEntry mobs = mob.getPokedexEntry();
       
        if ((mobs == IceFace.Ice || mobs == IceFace.noIce))
        {
	        if (attack.getCategory() == Category.PHYSICAL)
	        {
	            if (mobs == IceFace.Ice) mob.setPokedexEntry(IceFace.noIce);
	        }
        else if (mobs == IceFace.noIce &&  effects.getEffect(
                PokemobTerrainEffects.EFFECT_WEATHER_HAIL) > 0) mob.setPokedexEntry(IceFace.Ice);
        }
    }
}