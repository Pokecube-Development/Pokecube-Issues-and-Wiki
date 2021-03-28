package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Cobalion extends AbstractTypedCondition
{
    public Cobalion()
    {
        super("steel", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("cobalion");
    }
}
