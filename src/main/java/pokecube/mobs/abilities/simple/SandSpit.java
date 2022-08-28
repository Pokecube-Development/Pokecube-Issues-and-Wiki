package pokecube.mobs.abilities.simple;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;

public class SandSpit extends Ability
{

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final IPokemob attacker = move.attacker;
        final Level world = mob.getEntity().getLevel();

        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, new Vector3());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");

        if (attacker == mob || move.pre || attacker == move.attacked) return;
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
