package pokecube.api.data.effects.actions;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.PokemobCaps;

public class HappinessAction implements IEffectAction
{
    int amount = 10;

    public HappinessAction()
    {}

    @Override
    public void applyEffect(LivingEntity mob)
    {
        var pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob != null) pokemob.addHappiness(amount);
    }
}
