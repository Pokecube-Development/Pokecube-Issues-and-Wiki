package pokecube.core.moves.templates;

import java.util.Random;

import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class Move_Terrain extends Move_Basic
{

    public int effect;
    public int       duration = 300;

    /**
     * See TerrainSegment for the types of effects.
     *
     * @param name
     * @param effect
     */
    public Move_Terrain(final String name)
    {
        super(name);
        this.effect = this.move.baseEntry.extraInfo;
    }

    @Override
    /**
     * Called after the attack for special post attack treatment.
     *
     * @param attacker
     * @param attacked
     * @param f
     * @param finalAttackStrength
     *            the number of HPs the attack takes from target
     */
    public void doWorldAction(final IPokemob attacker, final Vector3 location)
    {
        if (attacker.getMoveStats().SPECIALCOUNTER > 0) return;
        attacker.getMoveStats().SPECIALCOUNTER = 20;

        this.duration = 300 + new Random().nextInt(600);
        final World world = attacker.getEntity().getEntityWorld();
        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, location);

        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");
        // TODO check if effect already exists, and send message if so.
        // Otherwise send the it starts to effect message

        teffect.setTerrainEffectDuration(PokemobTerrainEffects.WeatherEffectType.values()[this.effect], this.duration + world.getGameTime(), attacker);

        if (attacker.getEntity().isServerWorld()) PacketSyncTerrain.sendTerrainEffects(attacker.getEntity(),
                segment.chunkX, segment.chunkY, segment.chunkZ, teffect);

    }

    public void setDuration(final int duration)
    {
        this.duration = duration;
    }
}
