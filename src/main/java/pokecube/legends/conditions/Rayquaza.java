package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;

public class Rayquaza extends AbstractCondition
{
    @Override
    public boolean canCapture(final Entity trainer, final boolean message)
    {
        if (!super.canCapture(trainer, message)) return false;
        final boolean kyogre = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "kyogre")) > 0;
        final boolean groudon = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "groudon")) > 0;

        final String name = "Kyogre, Groudon";

        if (kyogre && groudon) return true;
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
        return Database.getEntry("rayquaza");
    }

}
