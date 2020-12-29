package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Tornadus extends AbstractEntriedCondition
{
    public Tornadus()
    {
        super("meloetta_aria");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("tornadus incarnate");
    }
}
