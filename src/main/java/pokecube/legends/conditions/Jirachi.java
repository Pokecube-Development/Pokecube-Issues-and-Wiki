package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Jirachi extends AbstractTypedCondition
{
    public Jirachi()
    {
        super("steel", 0.2f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("jirachi");
    }

}
