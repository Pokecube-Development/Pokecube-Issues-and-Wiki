package pokecube.mobs.abilities.simple;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.Tracker;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AbilityProvider(name = "sand-spit")
public class SandSpit extends Ability
{

    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        // We can be target and user at the same time, if self move.
        if (areWeTarget(mob, move)) return;

        final Level world = mob.getEntity().getLevel();

        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, new Vector3());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");

        if (move.hit)
        {
            // terrain.doWorldAction(mob, location);
            int duration = 300 + ThutCore.newRandom().nextInt(600);
            teffect.setEffectDuration(PokemobTerrainEffects.WeatherEffectType.SAND,
                    duration + Tracker.instance().getTick(), mob);

            if (world instanceof ServerLevel level)
                PacketSyncTerrain.sendTerrainEffects(level, segment.chunkX, segment.chunkY, segment.chunkZ, teffect);
        }
    }
}
