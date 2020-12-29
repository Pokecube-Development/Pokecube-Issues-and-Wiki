package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Raikou extends AbstractTypedCondition
{
    public Raikou()
    {
        super("electric");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("raikou");
    }
}
