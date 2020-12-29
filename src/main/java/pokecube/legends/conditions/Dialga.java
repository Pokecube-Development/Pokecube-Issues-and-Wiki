package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Dialga extends AbstractEntriedCondition
{
    public Dialga()
    {
        super("uxie", "mesprit", "azelf");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("dialga");
    }

}
