package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.Entity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "forcast")
public class Forecast extends Ability
{
    static PokedexEntry rain;
    static PokedexEntry sun;
    static PokedexEntry snow;
    static PokedexEntry base;

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getPokedexNb() != 351) return;// Only affect castform.

        final Entity pokemob = mob.getEntity();
        if (pokemob.tickCount % 20 != 9) return;// Only check once per
                                                   // second.

        if (Forecast.base == null) Forecast.base = Database.getEntry("castform");
        if (Forecast.sun == null) Forecast.sun = Database.getEntry("castform-sunny");
        if (Forecast.rain == null) Forecast.rain = Database.getEntry("castform-rainy");
        if (Forecast.snow == null) Forecast.snow = Database.getEntry("castform-snowy");

        // TODO check for weather canceling effects in the area, then set to
        // base and return early.

        final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(pokemob);
        final PokemobTerrainEffects effect = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");

        if (effect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.RAIN))
        {
            mob.setPokedexEntry(Forecast.rain);
            return;
        }

        if (effect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN))
        {
            mob.setPokedexEntry(Forecast.sun);
            return;
        }

        if (effect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.HAIL))
        {
            mob.setPokedexEntry(Forecast.snow);
            return;
        }
        mob.setPokedexEntry(Forecast.base);
    }

}
