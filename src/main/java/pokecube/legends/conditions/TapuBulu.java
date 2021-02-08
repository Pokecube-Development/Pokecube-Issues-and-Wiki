package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class TapuBulu extends AbstractTypedCondition
{
    public TapuBulu()
    {
        super("grass", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("tapu_bulu");
    }

}
