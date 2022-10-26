package pokecube.mobs.abilities.simple;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;

@AbilityProvider(name = "battle-bond")
public class BattleBond extends Ability
{
    /**
     * Called when a pokemob tries to mega evolve.
     *
     * @param mob
     */
    @Override
    public boolean canChange(IPokemob mob, PokedexEntry changeTo)
    {
        return true;
    }
}
