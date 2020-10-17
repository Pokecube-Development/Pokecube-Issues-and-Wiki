package pokecube.mobs.abilities.a;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/**
 *   Wiki description
 * When a Pokémon with Air Lock is in battle, all effects of weather are negated (though the weather itself does not disappear).
 **/

//TODO When a Pokémon with Air Lock is brought out, the message "The effects of weather disappeared." will be displayed after the Ability is announced.

public class Airlock extends Ability
{
    @Override
    public void preMove(IPokemob mob, MovePacket move)
    {
        final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
        teffect.setEffectDuration(PokemobTerrainEffects.NoEffects.NO_EFFECTS, 0, mob);
    }

    @Override
    public void postMove(IPokemob mob, MovePacket move)
    {
        removeEffect(mob);
    }

    @Override
    public IPokemob onRecall(IPokemob mob)
    {
        removeEffect(mob);
        return mob;
    }

    private void removeEffect(IPokemob mob)
    {
        final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
        teffect.removeEffect(PokemobTerrainEffects.NoEffects.NO_EFFECTS);
    }
}
