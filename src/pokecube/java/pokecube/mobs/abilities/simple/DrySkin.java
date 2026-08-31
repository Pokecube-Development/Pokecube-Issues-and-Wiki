package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "dry-skin")
public class DrySkin extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (mob.getEntity() == move.getTarget() && move.type == PokeType.getType("water"))
        {
            move.canceled = true;
            final LivingEntity entity = mob.getEntity();
            final float hp = entity.getHealth();
            final float maxHp = entity.getMaxHealth();
            entity.setHealth(Math.min(hp + 0.25f * maxHp, maxHp));
        }

        if (areWeUser(mob, move)) return;
        if (move.type == PokeType.getType("fire"))
        {
            move.pwr *= 1.25;
        }
    }

    // Heal 1/8th of max health in rain, lose 1/8th of max health in sun
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
        else if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.RAIN)) {
            user.heal(Math.min(user.getMaxHealth() / 8.0f, user.getMaxHealth() - user.getHealth()));
        }

    }

}
