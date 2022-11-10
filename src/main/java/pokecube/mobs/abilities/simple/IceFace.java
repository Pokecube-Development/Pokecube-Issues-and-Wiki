package pokecube.mobs.abilities.simple;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants.AttackCategory;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.database.Database;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "ice-face")
public class IceFace extends Ability
{
	private static PokedexEntry Ice;
    private static PokedexEntry noIce;
    
    private static boolean      noTurnBase = false;

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (IceFace.noTurnBase) return;
        if (IceFace.Ice == null)
        {
        	IceFace.Ice = Database.getEntry("eiscue-ice");
        	IceFace.noIce = Database.getEntry("eiscue-noice");
        	IceFace.noTurnBase = IceFace.Ice == null || IceFace.noIce == null; 
            if (IceFace.noTurnBase) return;
        }
        
        final MoveEntry attack = move.getMove();
        final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects effects = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
        final PokedexEntry mobs = mob.getPokedexEntry();
       
        if ((mobs == IceFace.Ice || mobs == IceFace.noIce))
        {
	        if (attack.getCategory() == AttackCategory.PHYSICAL)
	        {
	            if (mobs == IceFace.Ice) mob.setPokedexEntry(IceFace.noIce);
	        }
        else if (mobs == IceFace.noIce &&  effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.HAIL))
            mob.setPokedexEntry(IceFace.Ice);
        }
    }
}