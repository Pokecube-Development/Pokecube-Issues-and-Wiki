package pokecube.mobs.abilities.simple;

import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "air-lock")
public class Airlock extends Ability
{
    @Override
    public void startCombat(final IPokemob mob)
    {
        final Level world = mob.getEntity().level();
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

        if (world instanceof ServerLevel serverWorld)
            serverWorld.setWeatherParameters(0, 0, false, false);
        teffect.setEffectDuration(PokemobTerrainEffects.WeatherEffectType.SUN, 0, mob);
    }
}
