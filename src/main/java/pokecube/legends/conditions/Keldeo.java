package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;

public class Keldeo extends Condition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        boolean virizion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("virizion")) > 0;
        boolean terrakion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("terrakion")) > 0;
        boolean cobalion = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("cobalion")) > 0;
                
        String name = "Virizion, Terrakion, Cobalion";
                
        if ((virizion && terrakion && cobalion)) return true;
        if (pokemon != null && !trainer.getEntityWorld().isRemote)
        {
            sendNoTrust(trainer);
            sendLegendExtra(trainer, name);
        }
        return false;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("keldeo");
    }

}
