package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Diancie extends AbstractTypedCondition
{
    public Diancie()
    {
        super("fairy", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("diancie");
    }
}
