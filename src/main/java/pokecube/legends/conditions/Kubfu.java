package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Kubfu extends AbstractTypedCondition
{
    public Kubfu()
    {
        super("fighting", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("kubfu");
    }
}
