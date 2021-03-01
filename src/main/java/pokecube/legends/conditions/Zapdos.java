package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Zapdos extends AbstractTypedCondition
{
    public Zapdos()
    {
        super("electric", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("zapdos");
    }
}
