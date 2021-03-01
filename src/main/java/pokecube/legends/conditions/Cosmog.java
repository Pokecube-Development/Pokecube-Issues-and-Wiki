package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Cosmog extends AbstractTypedCondition
{
    public Cosmog()
    {
        super("psychic", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("cosmog");
    }

}
