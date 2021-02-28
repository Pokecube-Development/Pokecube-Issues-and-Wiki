package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Deoxys extends AbstractTypedCondition
{
    public Deoxys()
    {
        super("psychic", 0.2f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("deoxys");
    }

}
