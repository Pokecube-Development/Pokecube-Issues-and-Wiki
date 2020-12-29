package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Keldeo extends AbstractEntriedCondition
{
    public Keldeo()
    {
        super("virizion", "terrakion", "cobalion");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("keldeo");
    }
}
