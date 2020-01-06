package pokecube.core.database.abilities.b;

import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;

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
