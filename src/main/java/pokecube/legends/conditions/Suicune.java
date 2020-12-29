package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Suicune extends AbstractTypedCondition
{
    public Suicune()
    {
        super("water");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("suicune");
    }

}
