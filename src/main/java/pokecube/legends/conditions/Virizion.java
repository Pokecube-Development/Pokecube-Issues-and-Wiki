package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Virizion extends AbstractTypedCondition
{
    public Virizion()
    {
        super("grass", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("virizion");
    }
}
