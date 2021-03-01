package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Articuno extends AbstractTypedCondition
{
    public Articuno()
    {
        super("ice", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("articuno");
    }
}
