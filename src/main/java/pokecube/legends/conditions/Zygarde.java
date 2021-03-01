package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Zygarde extends AbstractTypedCondition
{
    public Zygarde()
    {
        super("ground", 0.2f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("zygarde_10");
    }

}
