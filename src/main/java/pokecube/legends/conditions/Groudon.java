package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.utils.PokeType;

public class Groudon extends AbstractCondition
{
    @Override
    public boolean canCapture(final Entity trainer, final boolean message)
    {
        if (!super.canCapture(trainer, message)) return false;
        final int count1 = CaptureStats.getUniqueOfTypeCaughtBy(trainer.getUniqueID(), PokeType.getType("ground"));
        final int count2 = KillStats.getUniqueOfTypeKilledBy(trainer.getUniqueID(), PokeType.getType("water"));
        final int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.getType("ground"));
        final int count4 = SpecialCaseRegister.countSpawnableTypes(PokeType.getType("water"));
        final double captureFactor = (double) count1 / (double) count3;
        final double killFactor = (double) count2 / (double) count4;

        final double roundCap = Math.round(captureFactor * 100.0) / 100.0;
        final double roundKill = Math.round(killFactor * 100.0) / 100.0;

        final float numTotal = 0.5f;
        final float numKill = 0.5f;

        final String type = "Ground";
        final String kill = "Water";

        if (roundKill >= numKill && roundCap >= numTotal) return true;
        if (!trainer.getEntityWorld().isRemote && message)
        {
            this.sendNoTrust(trainer);
            this.sendLegendDuo(trainer, type, kill, (int) (numTotal * count3), count1, (int) (numKill * count4),
                    count2);
        }
        return false;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("groudon");
    }

}
