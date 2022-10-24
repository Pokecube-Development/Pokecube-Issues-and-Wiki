package pokecube.core.moves.templates;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.PokemobTerrainEffects.EffectType;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;

public class Move_Terrain extends Move_Basic
{

    EffectType effect;
    public int duration = 300;

    /**
     * See TerrainSegment for the types of effects.
     *
     * @param name
     * @param effect
     */
    public Move_Terrain(final String name)
    {
        super(name);
        this.effect = PokemobTerrainEffects.getForIndex(this.move.root_entry._effect_index);
        if (PokecubeMod.debug)
            PokecubeAPI.LOGGER.info(name + " " + this.move.root_entry._effect_index + " " + this.effect);
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        final IPokemob attacker = packet.attacker;
        super.postAttack(packet);

        if (attacker.getMoveStats().SPECIALCOUNTER > 0 || packet.canceled) return;
        attacker.getMoveStats().SPECIALCOUNTER = 20;

        this.duration = 300 + ThutCore.newRandom().nextInt(600);
        final Level world = attacker.getEntity().getLevel();
        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world,
                new Vector3(attacker.getEntity()));

        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");
        // TODO check if effect already exists, and send message if so.
        // Otherwise send the it starts to effect messaged

        teffect.setEffectDuration(this.effect, this.duration + Tracker.instance().getTick(), attacker);
        if (world instanceof ServerLevel) PacketSyncTerrain.sendTerrainEffects((ServerLevel) world, segment.chunkX,
                segment.chunkY, segment.chunkZ, teffect);
    }

    public void setDuration(final int duration)
    {
        this.duration = duration;
    }
}
