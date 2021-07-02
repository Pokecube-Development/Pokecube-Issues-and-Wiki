package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Azelf extends AbstractTypedCondition
{
    public Azelf()
    {
        super("psychic", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("azelf");
    }
}
