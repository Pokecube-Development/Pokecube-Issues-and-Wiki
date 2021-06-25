package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Manaphy extends AbstractTypedCondition
{
    public Manaphy()
    {
        super("water", 0.4f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("manaphy");
    }
}
