package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;

public class Thundurus extends AbstractCondition
{
    @Override
    public boolean canCapture(final Entity trainer, final boolean message)
    {
        if (!super.canCapture(trainer, message)) return false;
        final boolean meloetta = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "meloetta_aria")) > 0;

        final String name = "meloetta_aria";

        if (meloetta) return true;
        if (!trainer.getEntityWorld().isRemote && message)
        {
            this.sendNoTrust(trainer);
            this.sendLegendExtra(trainer, name);
        }
        return false;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("thundurus incarnate");
    }

}
