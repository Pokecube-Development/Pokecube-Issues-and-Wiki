package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class TapuKoko extends AbstractTypedCondition
{
    public TapuKoko()
    {
        super("electric", 0.2f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("tapu_koko");
    }

}
