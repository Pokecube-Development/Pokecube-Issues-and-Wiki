package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;

public class Kyurem extends Condition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        boolean reshiram = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("reshiram")) > 0;
        boolean zekrom = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("zekrom")) > 0;
                
        String name = "Reshiram, Zekrom";
                
        if ((reshiram && zekrom)) return true;
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
        return Database.getEntry("kyurem");
    }

}
