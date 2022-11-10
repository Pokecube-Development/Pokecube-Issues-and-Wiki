package pokecube.core.moves.templates;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import pokecube.api.data.moves.IMove;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.PokemobTerrainEffects.EffectType;
import pokecube.core.moves.PokemobTerrainEffects.EntryEffectType;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.Tracker;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class TerrainMove implements IMove
{
    private static final Map<EffectType, TerrainMove> DEFAULTS = Maps.newHashMap();

    public static TerrainMove forEffect(EffectType type)
    {
        return DEFAULTS.computeIfAbsent(type, t -> new TerrainMove(t));
    }

    private final EffectType effect;
    public int duration = 300;

    private TerrainMove(EffectType effect)
    {
        this.effect = effect;
    }

    PostMoveUse postUse = new PostMoveUse()
    {
    };

    @Override
    public PostMoveUse getPostUse(MoveApplication t)
    {
        IPokemob user = t.getUser();
        if (user.getMoveStats().SPECIALCOUNTER > 0 || t.canceled) return null;
        user.getMoveStats().SPECIALCOUNTER = 20;

        this.duration = 300 + ThutCore.newRandom().nextInt(600);
        final Level world = user.getEntity().getLevel();
        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, new Vector3(user.getEntity()));

        EffectType apply = this.effect;
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");
        // TODO check if effect already exists, and send message if so.
        // Otherwise send the it starts to effect messaged

        // This one has additional layering effect up to POISON2
        if (effect == EntryEffectType.POISON && teffect.isEffectActive(apply))
        {
            apply = EntryEffectType.POISON2;
        }

        teffect.setEffectDuration(apply, this.duration + Tracker.instance().getTick(), user);
        if (world instanceof ServerLevel) PacketSyncTerrain.sendTerrainEffects((ServerLevel) world, segment.chunkX,
                segment.chunkY, segment.chunkZ, teffect);
        return postUse;
    }

    public void setDuration(final int duration)
    {
        this.duration = duration;
    }
}
