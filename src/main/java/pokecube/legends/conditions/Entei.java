package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Entei extends AbstractTypedCondition
{
    public Entei()
    {
        super("fire", 0.6f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("entei");
    }
}
