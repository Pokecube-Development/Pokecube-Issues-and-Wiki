package pokecube.legends.conditions;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.database.Database;

import java.util.Collections;
import java.util.List;

public abstract class AbstractTypedCondition extends AbstractCondition
{
    public PokeType type;
    public String name;
    public float threshold;

    protected AbstractTypedCondition(final String name, final String type, final float threshold)
    {
        this.type = PokeType.getType(type);
        this.threshold = threshold;
        this.name = name;
    }

    @Override
    public final PokedexEntry getEntry()
    {
        return Database.getEntry(this.name);
    }

    @Override
    protected boolean hasRequirements(final Entity trainer)
    {
        int count1 = this.caughtNumber(trainer, this.type);

        // special case for abolute number requirements
        if (Math.abs(this.threshold) > 1)
        {
            int n = (int) this.threshold;
            List<PokedexEntry> entries = type == PokeType.unknown
                    ? Database.spawnables
                    : Database.spawnablesByType.getOrDefault(type, Collections.emptyList());
            if (n < 0) n = entries.size() + n;
            if (n >= 0 && n < entries.size())
            {
                return count1 > n;
            }
            return false;
        }

        int count2 = this.spawnNumber(this.type);
        final double captureFactor = (double) count1 / (double) count2;
        final double roundOff = Math.round(captureFactor * 100.0) / 100.0;
        return roundOff >= this.threshold;
    }

    @Override
    public MutableComponent getFailureMessage(final Entity trainer)
    {
        if (customFailMesg != null) return super.getFailureMessage(trainer);
        final int count1 = this.caughtNumber(trainer, this.type);
        final int count2 = this.spawnNumber(this.type);
        return this.sendNoTrust(trainer).append("\n")
                .append(this.sendLegend(trainer, this.type.name, (int) (count2 * this.threshold), count1));
    }

}
