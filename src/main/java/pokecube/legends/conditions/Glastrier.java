package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Glastrier extends AbstractEntriedCondition
{
    public Glastrier()
    {
        super("calyrex");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("glastrier");
    }
}
