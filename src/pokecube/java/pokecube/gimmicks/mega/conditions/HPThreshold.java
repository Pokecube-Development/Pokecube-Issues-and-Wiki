package pokecube.gimmicks.mega.conditions;

import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;

public class HPThreshold implements MegaCondition
{
    // Percentage threshold
    public int threshold;

    @Override
    public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
    {
        int percentage = (int)(mobIn.getHealth() * 100 / mobIn.getMaxHealth());
        return percentage <= threshold;
    }
}

