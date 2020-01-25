package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class Yveltal extends Condition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        int count1 = CaptureStats.getUniqueOfTypeCaughtBy(trainer.getUniqueID(), PokeType.getType("dark"));
        int count2 = SpecialCaseRegister.countSpawnableTypes(PokeType.getType("dark"));
        double captureFactor = ((double)count1 / (double)count2);
        double roundOff = Math.round(captureFactor * 100.0) / 100.0;
        
        float numTotal = 0.5f;
        String type = "Dark";
        
        if (roundOff >= numTotal) { return true; }
        if (pokemon != null && !trainer.getEntityWorld().isRemote)
        {
            sendNoTrust(trainer);
            sendLegend(trainer, type, numTotal, roundOff);
        }
        return false;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("yveltal");
    }

}
