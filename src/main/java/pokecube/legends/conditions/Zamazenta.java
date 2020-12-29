package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Zamazenta extends AbstractTypedCondition
{
    public Zamazenta()
    {
        super("steel", 0.6f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("zamazenta");
    }

}
