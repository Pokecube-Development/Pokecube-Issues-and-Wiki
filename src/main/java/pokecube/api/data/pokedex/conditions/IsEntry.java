package pokecube.api.data.pokedex.conditions;

import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;

public class IsEntry implements PokemobCondition
{
    public String entry;

    private PokedexEntry _entry;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return mobIn.getPokedexEntry() == _entry;
    }

    @Override
    public void init()
    {
        this._entry = Database.getEntry(entry);
    }
}
