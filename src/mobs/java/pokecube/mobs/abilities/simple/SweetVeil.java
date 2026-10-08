package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.moves.damage.effects.StatusEffects;

@AbilityProvider(name = "sweet-veil")
public class SweetVeil extends Ability
{
    @Override
    public void onUpdate(final IPokemob mob)
    {
        if (mob.getEntity().hasEffect(StatusEffects.SLEEP)) mob.healStatus();
    }
}
