package pokecube.mobs.abilities.simple;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.Tracker;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.core.common.network.TerrainUpdate;

@AbilityProvider(name = "sand-stream")
public class SandStream extends Ability
{

    @Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {
        if (target != null) // Only trigger if against a pokemob.
        {
            final Level world = mob.getEntity().level();
            final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
            final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

            int duration = 300 + ThutCore.newRandom().nextInt(600);
            teffect.setEffectDuration(PokemobTerrainEffects.WeatherEffectType.SAND,
                    duration + Tracker.instance().getTick(), mob);
            if (world instanceof ServerLevel) TerrainUpdate.sendTerrainToWatching(segment);
        }
    }
}