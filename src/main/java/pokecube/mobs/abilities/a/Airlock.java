package pokecube.mobs.abilities.a;

import net.minecraft.world.World;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class Airlock extends Ability
{
    /*@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (!move.pre) return;

        final World world = mob.getEntity().getEntityWorld();
        boolean rain = world.isRaining();
        if (!rain)
        {
           /* final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
            final PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
            teffect.
        }
    }*/
}
