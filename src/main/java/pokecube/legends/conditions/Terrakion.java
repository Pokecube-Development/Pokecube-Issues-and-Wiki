package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Terrakion extends AbstractTypedCondition
{
    public Terrakion()
    {
        super("rock", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("terrakion");
    }
}
