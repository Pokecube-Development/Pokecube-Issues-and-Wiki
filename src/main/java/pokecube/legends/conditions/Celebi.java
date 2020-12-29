package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Celebi extends AbstractTypedCondition
{
    protected Celebi()
    {
        super("grass", 0.7f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("celebi");
    }
}
