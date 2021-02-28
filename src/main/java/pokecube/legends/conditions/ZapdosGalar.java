package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class ZapdosGalar extends AbstractTypedCondition
{
    public ZapdosGalar()
    {
        super("fighting", 0.2f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("zapdos galar");
    }
}
