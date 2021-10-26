package pokecube.mobs.abilities.a;

import net.minecraft.world.level.Level;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class Airlock extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (!move.pre) return;

        final Level world = mob.getEntity().getCommandSenderWorld();
        final boolean rain = world.isRaining();
        if (!rain)
        {
           /* final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
            final PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
            teffect.*/
        }
    }
}
