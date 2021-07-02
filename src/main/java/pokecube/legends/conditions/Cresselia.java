package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Cresselia extends AbstractTypedCondition
{
    public Cresselia()
    {
        super("fairy", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("cresselia");
    }
}
