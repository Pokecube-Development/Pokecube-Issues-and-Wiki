package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;

public class Hooh extends Condition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        boolean raikou = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("raikou")) > 0;
        boolean suicune = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("suicune")) > 0;
        boolean entei = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("entei")) > 0;

        String name = "Raikou, Suicune, Entei";
                
        if ((raikou && entei && suicune)) return true;
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
        return Database.getEntry("hooh");
    }

}
