package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Palkia extends AbstractEntriedCondition
{
    public Palkia()
    {
        super("uxie", "mesprit", "azelf");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("palkia");
    }

}
