package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;

public class Eternatus extends AbstractCondition
{
    @Override
    public boolean canCapture(final Entity trainer, final boolean message)
    {
        if (!super.canCapture(trainer, message)) return false;
        final boolean dialga = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "zacian")) > 0;
        final boolean palkia = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "zamazenta")) > 0;

        final String name = "Zacian, Zamazenta";

        if (dialga && palkia) return true;
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
        return Database.getEntry("eternatus");
    }

}
