package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;

@AbilityProvider(name = "natural-cure")
public class NaturalCure extends Ability
{
    @Override
    public void onRecall(final IPokemob mob)
    {
        mob.healStatus();
    }
}