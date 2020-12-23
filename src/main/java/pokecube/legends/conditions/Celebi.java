package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.utils.PokeType;

public class Celebi extends AbstractCondition
{
    @Override
    public boolean canCapture(final Entity trainer, final boolean message)
    {
        if (!super.canCapture(trainer, message)) return false;
        final int count1 = CaptureStats.getUniqueOfTypeCaughtBy(trainer.getUniqueID(), PokeType.getType("grass"));
        final int count2 = SpecialCaseRegister.countSpawnableTypes(PokeType.getType("grass"));
        final double captureFactor = (double) count1 / (double) count2;
        final double roundOff = Math.round(captureFactor * 100.0) / 100.0;

        final float numTotal = 0.7f;
        final String type = "Grass";

        if (roundOff >= numTotal) return true;
        if (!trainer.getEntityWorld().isRemote) if (roundOff < numTotal)
        {
            this.sendNoTrust(trainer);
            this.sendLegend(trainer, type, (int) (count2 * numTotal), count1);
        }
        return false;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("celebi");
    }

}
