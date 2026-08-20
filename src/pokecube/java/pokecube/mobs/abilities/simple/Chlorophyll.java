package pokecube.mobs.abilities.simple;

import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;

@AbilityProvider(name = "chlorophyll")
public class Chlorophyll extends Ability
{
    @Override
    // Apply speed increase in the sun.
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final Level world = mob.getEntity().level();
        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, new Vector3());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

        if (!areWeUser(mob, move)) return;

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN) && !mob.getEntity().getPersistentData().contains("pokecube:ChlorophyllActive"))
        {
            MovesUtils.handleStats2(mob, mob.getEntity(), IMoveConstants.VIT, IMoveConstants.RAISE);
            mob.getEntity().getPersistentData().putBoolean("pokecube:ChlorophyllActive", true);
        }

    }

    @Override
    public void endCombat(IPokemob mob)
    {
        mob.getEntity().getPersistentData().remove("pokecube:ChlorophyllActive");
    }

    @Override
    public void onRecall(IPokemob mob) { mob.getEntity().getPersistentData().remove("pokecube:ChlorophyllActive"); }
}
