package pokecube.gimmicks.mega.conditions;

import net.minecraft.core.HolderLookup.Provider;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.conditions.HasAbility;
import pokecube.api.entity.pokemob.IPokemob;

public class Ability extends HasAbility implements MegaCondition
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
