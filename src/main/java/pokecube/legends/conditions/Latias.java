package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Latias extends AbstractTypedCondition
{
    public Latias()
    {
        super("dragon", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("latias");
    }
}
