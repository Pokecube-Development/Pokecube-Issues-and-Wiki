package pokecube.gimmicks.mega.conditions;

import net.minecraft.core.HolderLookup;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;

public class HPThreshold implements MegaCondition
{
    // Percentage threshold
    public int threshold = 0;
    public int _threshold = 0;

    @Override
    public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
    {
        int percentage = (int)(mobIn.getHealth() * 100 / mobIn.getMaxHealth());
        return percentage <= _threshold;
    }

    @Override
    public void init(HolderLookup.Provider registries) { _threshold = threshold; }
}

