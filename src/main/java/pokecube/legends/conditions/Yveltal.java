package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Yveltal extends AbstractTypedCondition
{
    public Yveltal()
    {
        super("dark");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("yveltal");
    }

}
