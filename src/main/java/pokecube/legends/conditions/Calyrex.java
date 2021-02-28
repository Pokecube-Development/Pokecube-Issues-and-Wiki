package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Calyrex extends AbstractTypedCondition
{
    public Calyrex()
    {
        super("grass", 0.4f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("calyrex");
    }

}
