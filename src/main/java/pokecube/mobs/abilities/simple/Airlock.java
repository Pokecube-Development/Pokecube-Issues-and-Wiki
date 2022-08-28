package pokecube.mobs.abilities.simple;

import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class Airlock extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (!move.pre) return;

        final Level world = mob.getEntity().getLevel();
        final boolean rain = world.isRaining();
        if (!rain)
        {
           /* final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
            final PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
            teffect.*/
        }
    }
}
