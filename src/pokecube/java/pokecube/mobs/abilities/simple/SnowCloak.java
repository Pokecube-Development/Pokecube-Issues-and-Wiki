package pokecube.mobs.abilities.simple;

import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;

@AbilityProvider(name = "snow-cloak")
public class SnowCloak extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final Level world = mob.getEntity().level();
        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, new Vector3());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");


        if (!areWeUser(mob, move)) return;

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.HAIL) && !mob.getEntity().getPersistentData().contains("pokecube:SnowCloakActive"))
        {
            int boost = IMoveConstants.EVASION;
            MovesUtils.handleStats2(mob, mob.getEntity(), boost, IMoveConstants.RAISE);
            mob.getEntity().getPersistentData().putBoolean("pokecube:SnowCloakActive", true);
        } else {
            int drop = IMoveConstants.EVASION;
            MovesUtils.handleStats2(mob, mob.getEntity(), drop, IMoveConstants.FALL);
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