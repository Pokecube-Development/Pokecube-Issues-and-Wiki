package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Landorus extends AbstractEntriedCondition
{
    public Landorus()
    {
        super("meloetta_aria");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("landorus incarnate");
    }

}
