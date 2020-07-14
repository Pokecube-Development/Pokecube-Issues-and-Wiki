package pokecube.mobs.abilities.m;

import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.utils.PokeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class Mimicry extends Ability
{
    @Override
    public void onUpdate(final IPokemob mob)
    {
    	final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
    	final PokemobTerrainEffects effects = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
         
    	final PokedexEntry mobs = mob.getPokedexEntry();
    	if(!mob.inCombat()) 
        	mob.setPokedexEntry(mobs);
    	
        if (effects.getEffect(PokemobTerrainEffects.EFFECT_TERRAIN_ELECTRIC) > 0 && mob.getEntity().onGround) {
        	mob.setType1(PokeType.getType("electric"));
        }
        else if (effects.getEffect(PokemobTerrainEffects.EFFECT_TERRAIN_GRASS) > 0 && mob.getEntity().onGround) {
        	mob.setType1(PokeType.getType("grass"));
        }
        else if (effects.getEffect(PokemobTerrainEffects.EFFECT_TERRAIN_MISTY) > 0 && mob.getEntity().onGround) {
        	mob.setType1(PokeType.getType("fairy"));
        }
    }
    
    @Override
    public IPokemob onRecall(final IPokemob mob)
    {
        final PokedexEntry mobs = mob.getPokedexEntry();
        if (mob.getType1() != mobs.getType1()) return mob.setPokedexEntry(mobs);
        return super.onRecall(mob);
    }
}
