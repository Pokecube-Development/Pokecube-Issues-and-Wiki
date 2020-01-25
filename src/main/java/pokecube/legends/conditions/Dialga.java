package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;

public class Dialga extends Condition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        boolean uxie = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("uxie")) > 0;
        boolean mesprit = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("mesprit")) > 0;
        boolean azelf = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("azelf")) > 0;
        
        String name = "Uxie, Mesprir, Azelf";
                
        if ((uxie && mesprit && azelf)) return true;
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
        return Database.getEntry("dialga");
    }

}
