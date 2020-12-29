package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Reshiram extends AbstractTypedCondition
{
    public Reshiram()
    {
        super("dragon");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("reshiram");
    }

}
