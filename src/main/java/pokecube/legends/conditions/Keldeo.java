package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;

public class Keldeo extends AbstractCondition
{
    @Override
    public boolean canCapture(final Entity trainer, final boolean message)
    {
        if (!super.canCapture(trainer, message)) return false;
        final boolean virizion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "virizion")) > 0;
        final boolean terrakion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "terrakion")) > 0;
        final boolean cobalion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "cobalion")) > 0;

        final String name = "Virizion, Terrakion, Cobalion";

        if (virizion && terrakion && cobalion) return true;
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
        return Database.getEntry("keldeo");
    }

}
