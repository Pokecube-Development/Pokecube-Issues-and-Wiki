package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Uxie extends AbstractTypedCondition
{
    public Uxie()
    {
        super("psychic", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("uxie");
    }
}
