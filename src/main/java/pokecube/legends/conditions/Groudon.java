package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class Groudon extends AbstractCondition
{
    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("groudon");
    }

    @Override
    boolean hasRequirements(final Entity trainer)
    {
        final int count1 = this.caughtNumber(trainer, PokeType.getType("ground"));
        final int count2 = this.killedNumber(trainer, PokeType.getType("water"));
        final int count3 = this.spawnNumber(PokeType.getType("ground"));
        final int count4 = this.spawnNumber(PokeType.getType("water"));
        final double captureFactor = (double) count1 / (double) count3;
        final double killFactor = (double) count2 / (double) count4;
        final double roundCap = Math.round(captureFactor * 100.0) / 100.0;
        final double roundKill = Math.round(killFactor * 100.0) / 100.0;
        final float numTotal = 0.5f;
        final float numKill = 0.5f;
        if (roundKill >= numKill && roundCap >= numTotal) return true;
        return false;
    }

    @Override
    void sendFailureMessage(final Entity trainer)
    {
        final int count1 = this.caughtNumber(trainer, PokeType.getType("ground"));
        final int count2 = this.killedNumber(trainer, PokeType.getType("water"));
        final int count3 = this.spawnNumber(PokeType.getType("ground"));
        final int count4 = this.spawnNumber(PokeType.getType("water"));
        final float numTotal = 0.5f;
        final float numKill = 0.5f;
        final String type = "Ground";
        final String kill = "Water";
        this.sendNoTrust(trainer);
        this.sendLegendDuo(trainer, type, kill, (int) (numTotal * count3), count1, (int) (numKill * count4), count2);
    }

}
