package pokecube.mobs.abilities.simple;

import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "rain-dish")
public class RainDish extends Ability 
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final Level world = mob.getEntity().level();
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");
        var entity = mob.getEntity();

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.RAIN) || world.isRaining())
            entity.heal(Math.min(entity.getMaxHealth() / 16.0f, entity.getMaxHealth() - entity.getHealth()));
    }
}
