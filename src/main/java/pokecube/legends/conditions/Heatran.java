package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Heatran extends AbstractTypedCondition
{
    protected Heatran()
    {
        super("fire");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("heatran");
    }
}
