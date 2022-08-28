package pokecube.mobs.abilities.simple;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.PokemobTerrainEffects;
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

    	if(mob.getEntity().isOnGround()) {
            if (effects.isEffectActive(PokemobTerrainEffects.TerrainEffectType.ELECTRIC)) {
                mob.setType1(PokeType.getType("electric"));
            } else if (effects.isEffectActive(PokemobTerrainEffects.TerrainEffectType.GRASS)) {
                mob.setType1(PokeType.getType("grass"));
            } else if (effects.isEffectActive(PokemobTerrainEffects.TerrainEffectType.MISTY)) {
                mob.setType1(PokeType.getType("fairy"));
            }
        }
    }

    @Override
    public IPokemob onRecall(final IPokemob mob)
    {
        final PokedexEntry mobs = mob.getPokedexEntry();
        mob.setType1(mobs.getType1());
        return super.onRecall(mob);
    }
}
