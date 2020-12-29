package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.core.utils.PokeType;

public abstract class AbstractTypedCondition extends AbstractCondition
{
    private final String type;

    private final float threshold;

    protected AbstractTypedCondition(final String type, final float threshold)
    {
        this.type = type;
        this.threshold = threshold;
    }

    protected AbstractTypedCondition(final String type)
    {
        this(type, 0.5f);
    }

    @Override
    boolean hasRequirements(final Entity trainer)
    {
        final int count1 = this.caughtNumber(trainer, PokeType.getType(this.type));
        final int count2 = this.spawnNumber(PokeType.getType(this.type));
        final double captureFactor = (double) count1 / (double) count2;
        final double roundOff = Math.round(captureFactor * 100.0) / 100.0;
        final float numTotal = this.threshold;
        if (roundOff >= numTotal) return true;
        return false;
    }

    @Override
    void sendFailureMessage(final Entity trainer)
    {
        final int count1 = this.caughtNumber(trainer, PokeType.getType(this.type));
        final int count2 = this.spawnNumber(PokeType.getType(this.type));
        final float numTotal = this.threshold;
        this.sendNoTrust(trainer);
        this.sendLegend(trainer, this.type, (int) (count2 * numTotal), count1);
    }

}
