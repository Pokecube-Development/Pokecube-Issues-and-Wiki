package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Arceus extends AbstractEntriedCondition
{
    public Arceus()
    {
        super("dialga", "palkia", "giratina");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("arceus");
    }
}
