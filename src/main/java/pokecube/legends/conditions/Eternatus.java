package pokecube.legends.conditions;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Eternatus extends AbstractEntriedCondition
{
    protected Eternatus()
    {
        super("zacian", "zamazenta");
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("eternatus");
    }
}
