package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Magearna extends AbstractTypedCondition
{
    public Magearna()
    {
        super("ghost", 0.2f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("magearna");
    }

}
