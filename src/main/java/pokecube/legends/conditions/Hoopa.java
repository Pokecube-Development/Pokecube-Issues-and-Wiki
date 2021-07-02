package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Hoopa extends AbstractTypedCondition
{
    public Hoopa()
    {
        super("ghost", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("hoopa_confined");
    }

}
