package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Victini extends AbstractTypedCondition
{
    public Victini()
    {
        super("psychic");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("victini");
    }

}
