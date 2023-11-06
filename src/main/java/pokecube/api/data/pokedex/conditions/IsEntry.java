package pokecube.api.data.pokedex.conditions;

import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;

/**
 * This class matches a pokemob with the specified pokedex entry<br>
 * <br>
 * Matcher key: "entry" <br>
 * Json keys: <br>
 * "entry" - string, name of the pokedex entry to match
 */
@Condition(name="entry")
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
