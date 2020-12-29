package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Lugia extends AbstractEntriedCondition
{
    public Lugia()
    {
        super("articuno", "zapdos", "moltres");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("lugia");
    }
}
