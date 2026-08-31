package pokecube.mobs.abilities.simple;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.Tracker;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.core.common.ThutCore;
import thut.core.common.network.TerrainUpdate;

@AbilityProvider(name = "sand-spit")
public class SandSpit extends Ability
{

    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        // We can be target and user at the same time, if self move.
        if (areWeTarget(mob, move)) return;

        final Level world = mob.getEntity().level();

        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

        if (move.hit)
        {
            int duration = 300 + ThutCore.newRandom().nextInt(600);
            teffect.setEffectDuration(PokemobTerrainEffects.WeatherEffectType.SAND,
                    duration + Tracker.instance().getTick(), mob);
            if (world instanceof ServerLevel) TerrainUpdate.sendTerrainToWatching(segment);
        }
    }
}
