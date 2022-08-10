package pokecube.mobs.abilities.b;

import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.abilities.Ability;

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
