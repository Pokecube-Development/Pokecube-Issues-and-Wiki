package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class ArticunoGalar extends AbstractTypedCondition
{
    public ArticunoGalar()
    {
        super("flying", 0.3f);
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("articuno_galar");
    }
}
