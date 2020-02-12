package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class Xerneas extends Condition
{
    @Override
    public boolean canCapture(final Entity trainer, final IPokemob pokemon)
    {
        if (!this.canCapture(trainer)) return false;
        final int count1 = CaptureStats.getUniqueOfTypeCaughtBy(trainer.getUniqueID(), PokeType.getType("fairy"));
        final int count2 = SpecialCaseRegister.countSpawnableTypes(PokeType.getType("fairy"));
        final double captureFactor = (double) count1 / (double) count2;
        final double roundOff = Math.round(captureFactor * 100.0) / 100.0;

        final float numTotal = 0.4f;
        final String type = "Fairy";

        if (roundOff >= numTotal) return true;
        if (pokemon != null && !trainer.getEntityWorld().isRemote)
        {
            this.sendNoTrust(trainer);
            this.sendLegend(trainer, type, (int) (count2 * numTotal), count1);
        }
        return false;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("xerneas");
    }

}
