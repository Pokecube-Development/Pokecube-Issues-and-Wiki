package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class TapuLele extends AbstractTypedCondition
{
    public TapuLele()
    {
        super("psychic", 0.2f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("tapu_lele");
    }

}
