package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Spectrier extends AbstractEntriedCondition
{
    public Spectrier()
    {
        super("calyrex");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("spectrier");
    }

}
