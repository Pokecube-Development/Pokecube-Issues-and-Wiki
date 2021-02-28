package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Moltres extends AbstractTypedCondition
{
    public Moltres()
    {
        super("fire", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("moltres");
    }
}
