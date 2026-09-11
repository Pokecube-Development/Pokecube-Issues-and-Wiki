package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import pokecube.core.moves.damage.effects.StatusEffects;

@AbilityProvider(name = "marvel-scale")
public class MarvelScale extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;

        Mob pokemobEntity = mob.getEntity();
        var statusEffectInstance = StatusEffects.getStatusEffect(pokemobEntity);
        if (statusEffectInstance == null) return;
        var effect = statusEffectInstance.getEffect();

        if (StatusEffects.hasAnyStatusEffects(mob.getEntity()) && StatusEffects.isNonVolatile(effect))
        {
            var attr = mob.getEntity().getAttribute(PokecubeAttributes.DEFENSE);
            attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

    }
}
