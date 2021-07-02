package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Mesprit extends AbstractTypedCondition
{
    public Mesprit()
    {
        super("psychic", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("mesprit");
    }
}
