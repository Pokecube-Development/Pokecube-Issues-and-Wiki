package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Zekrom extends AbstractTypedCondition
{
    public Zekrom()
    {
        super("dragon");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("zekrom");
    }

}
