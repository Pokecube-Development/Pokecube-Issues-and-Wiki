package pokecube.mobs.abilities.s;

import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;

public class SandSpit extends Ability
{
    public int duration = 300 + ThutCore.newRandom().nextInt(600);

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final IPokemob attacker = move.attacker;
        final World world = mob.getEntity().getCommandSenderWorld();

        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, Vector3.getNewVector());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");

        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit)
        {
            // terrain.doWorldAction(mob, location);

            teffect.setEffectDuration(PokemobTerrainEffects.WeatherEffectType.SAND, this.duration + Tracker.instance()
                    .getTick(), mob);

            if (world instanceof ServerWorld) PacketSyncTerrain.sendTerrainEffects((ServerWorld) world, segment.chunkX,
                    segment.chunkY, segment.chunkZ, teffect);
        }
    }
}
