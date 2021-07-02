package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Meloetta extends AbstractTypedCondition
{
    public Meloetta()
    {
        super("normal", 0.4f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("meloetta_aria");
    }
}
