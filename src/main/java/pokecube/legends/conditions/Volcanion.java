package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Volcanion extends AbstractEntriedCondition
{
    public Volcanion()
    {
        super("magearna");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("volcanion");
    }

}
