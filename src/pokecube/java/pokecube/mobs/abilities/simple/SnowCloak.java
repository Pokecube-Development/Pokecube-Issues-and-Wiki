package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "snow-cloak")
public class SnowCloak extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");


        if (!areWeUser(mob, move)) return;

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.HAIL) && !mob.getEntity().getPersistentData().contains("pokecube:SnowCloakActive"))
        {
            MovesUtils.handleStats2(mob, mob.getEntity(), IMoveConstants.EVASION, IMoveConstants.RAISE);
            mob.getEntity().getPersistentData().putBoolean("pokecube:SnowCloakActive", true);
        } else if (mob.getEntity().getPersistentData().contains("pokecube:SnowCloakActive")) {
            MovesUtils.handleStats2(mob, mob.getEntity(), IMoveConstants.EVASION, IMoveConstants.FALL);
            mob.getEntity().getPersistentData().remove("pokecube:SnowCloakActive");
        }
    }

    @Override
    public void endCombat(IPokemob mob)
    {
        mob.getEntity().getPersistentData().remove("pokecube:SnowCloakActive");
    }

    @Override
    public void onRecall(IPokemob mob) { mob.getEntity().getPersistentData().remove("pokecube:SnowCloakActive"); }
}