package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Zacian extends AbstractTypedCondition
{
    public Zacian()
    {
        super("steel", 0.6f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("zacian");
    }

}
