package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Xerneas extends AbstractTypedCondition
{
    public Xerneas()
    {
        super("fairy", 0.4f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("xerneas");
    }

}
