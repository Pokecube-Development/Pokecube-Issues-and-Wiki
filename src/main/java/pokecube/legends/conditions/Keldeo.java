package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;

public class Keldeo extends Condition
{
    @Override
    public boolean canCapture(final Entity trainer, final IPokemob pokemon)
    {
        if (!super.canCapture(trainer, pokemon)) return false;
        final boolean virizion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "virizion")) > 0;
        final boolean terrakion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "terrakion")) > 0;
        final boolean cobalion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "cobalion")) > 0;

        final String name = "Virizion, Terrakion, Cobalion";

        if (virizion && terrakion && cobalion) return true;
        if (pokemon != null && !trainer.getEntityWorld().isRemote)
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
