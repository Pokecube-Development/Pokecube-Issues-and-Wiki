package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;

public class Lugia extends Condition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        boolean articuno = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("articuno")) > 0;
        boolean zapdos = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("zapdos")) > 0;
        boolean moltres = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("moltres")) > 0;
                
        String name = "Articuno, Zapdos, Moltres";
                
        if ((articuno && moltres && zapdos)) return true;
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
        return Database.getEntry("lugia");
    }

}
