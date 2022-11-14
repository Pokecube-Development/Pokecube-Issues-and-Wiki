package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.HealProvider;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@MoveProvider(name = "synthesis")
public class Synthesis implements HealProvider
{
    @Override
    public void applyHealing(Damage t)
    {
        var moveAppl = t.move();
        float max_hp = moveAppl.getUser().getMaxHealth();
        float current_hp = moveAppl.getUser().getHealth();
        float heal = getSelfHealRatio(t.move().getUser()) * max_hp / 100.0f;
        if (heal > 0)
        {
            heal = Math.min(max_hp - current_hp, heal);
            if (heal > 0) moveAppl.getUser().getEntity().heal(heal);
        }
    }

    public float getSelfHealRatio(IPokemob user)
    {
        final TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(user.getEntity());
        final PokemobTerrainEffects effects = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
        if (effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.RAIN)
                || effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.HAIL)
                || effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SAND))
            return 25f;
        if (effects.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN)) return 200 / 3f;
        return 50f;
    }

}
