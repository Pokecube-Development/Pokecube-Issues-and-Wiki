package pokecube.gimmicks.mega.conditions;

import net.minecraft.core.HolderLookup.Provider;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.conditions.HasMove;
import pokecube.api.entity.pokemob.IPokemob;

public class Move extends HasMove implements MegaCondition
{

    @Override
    public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
    {
        return super.matches(mobIn);
    }

    @Override
    public void init(Provider registries)
    {
        super.init(registries);
    }

}
