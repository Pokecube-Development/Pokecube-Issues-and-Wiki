package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Thundurus extends AbstractTypedCondition
{
    public Thundurus()
    {
        super("meloetta_aria");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("thundurus incarnate");
    }

}
