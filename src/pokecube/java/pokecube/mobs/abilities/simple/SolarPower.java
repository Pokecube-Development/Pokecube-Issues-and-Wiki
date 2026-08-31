package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.Mob;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "solar-power")
public class SolarPower extends Ability
{
    @Override
    // Apply 50% spA increase in the sun.
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

        if (!areWeUser(mob, move)) return;

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN) && move.hit && move.getMove().getCategory(move.getUser()) == IMoveConstants.AttackCategory.SPECIAL) move.pwr *= 1.5;

    }

    // Apply 1/8 of full health as damage.
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        // We can be target and user at the same time, if self move.
        if (areWeTarget(mob, move)) return;

        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");
        final Mob user = mob.getEntity();

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN)) {
            user.hurt(user.damageSources().fall(), Math.min(user.getMaxHealth() / 8.0f, user.getHealth()));
        }

    }
}
