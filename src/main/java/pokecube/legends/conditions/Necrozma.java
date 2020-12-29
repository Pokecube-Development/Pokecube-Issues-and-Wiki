package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Necrozma extends AbstractEntriedCondition
{
    public Necrozma()
    {
        super("solgaleo", "lunala");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("necrozma");
    }
}
