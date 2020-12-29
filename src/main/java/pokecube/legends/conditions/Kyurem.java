package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Kyurem extends AbstractEntriedCondition
{
    public Kyurem()
    {
        super("reshiram", "zekrom");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("kyurem");
    }
}
