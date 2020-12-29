package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Giratina extends AbstractEntriedCondition
{
    public Giratina()
    {
        super("dialga","palkia");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("giratina");
    }
}
