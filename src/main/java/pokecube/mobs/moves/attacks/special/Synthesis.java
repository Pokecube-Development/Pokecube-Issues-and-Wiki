package pokecube.mobs.moves.attacks.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class Synthesis extends Move_Basic
{

    public Synthesis()
    {
        super("synthesis");
    }

    @Override
    public float getSelfHealRatio(IPokemob user)
    {
        final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(user.getEntity());
        final PokemobTerrainEffects effects = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
        if (effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.RAIN) ||
                effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.HAIL) ||
                effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SAND)) return 25f;
        if (effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN)) return 200 / 3f;
        return 50f;
    }

}
