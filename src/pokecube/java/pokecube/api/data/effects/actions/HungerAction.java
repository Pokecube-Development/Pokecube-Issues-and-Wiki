package pokecube.api.data.effects.actions;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.PokemobCaps;

public class HungerAction implements IEffectAction
{
    int amount = 100;

    public HungerAction()
    {}

    @Override
    public void applyEffect(LivingEntity mob)
    {
        var pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob != null) pokemob.applyHunger(amount);
    }
}
