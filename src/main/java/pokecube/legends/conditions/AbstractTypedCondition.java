package pokecube.legends.conditions;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public abstract class AbstractTypedCondition extends AbstractCondition
{
    public final String type;
    public final String name;
    public final float threshold;

    protected AbstractTypedCondition(final String name, final String type, final float threshold)
    {
        this.type = type;
        this.threshold = threshold;
        this.name = name;
    }

    protected AbstractTypedCondition(final String name, final String type)
    {
        this(name, type, 0.5f);
    }

    @Override
    public final PokedexEntry getEntry()
    {
        return Database.getEntry(this.name);
    }

    @Override
    protected boolean hasRequirements(final Entity trainer)
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
    public MutableComponent getFailureMessage(final Entity trainer)
    {
        final int count1 = this.caughtNumber(trainer, PokeType.getType(this.type));
        final int count2 = this.spawnNumber(PokeType.getType(this.type));
        final float numTotal = this.threshold;
        return this.sendNoTrust(trainer).append("\n").append(this.sendLegend(trainer, this.type, (int) (count2 * numTotal), count1));
    }

}
