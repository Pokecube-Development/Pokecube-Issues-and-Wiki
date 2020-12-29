package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Rayquaza extends AbstractEntriedCondition
{
    public Rayquaza()
    {
        super("kyogre", "groudon");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("rayquaza");
    }

}
