package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class TapuFini extends AbstractTypedCondition
{
    public TapuFini()
    {
        super("water", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("tapu_fini");
    }

}
