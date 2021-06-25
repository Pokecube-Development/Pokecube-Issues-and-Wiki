package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Latios extends AbstractTypedCondition
{
    public Latios()
    {
        super("dragon", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("latios");
    }
}
