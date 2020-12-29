package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Hooh extends AbstractEntriedCondition
{
    public Hooh()
    {
        super("raikou", "suicune", "entei");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("hooh");
    }
}
