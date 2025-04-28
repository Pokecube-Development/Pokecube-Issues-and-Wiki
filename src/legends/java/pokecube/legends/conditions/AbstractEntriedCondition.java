package pokecube.legends.conditions;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;

import java.util.List;

public abstract class AbstractEntriedCondition extends AbstractCondition
{
    public final List<String> needed;

    String names;

    public AbstractEntriedCondition(final String... needed)
    {
        this.needed = Lists.newArrayList(needed);
    }

    @Override
    protected boolean hasRequirements(final Entity trainer)
    {
        for (final String s : this.needed)
            if (this.caughtNumber(trainer, Database.getEntry(s)) <= 0) return false;
        return true;
    }

    @Override
    public MutableComponent getFailureMessage(final Entity trainer)
    {
        if (customFailMesg != null) return super.getFailureMessage(trainer);
        if (this.names == null)
        {
            this.names = "";
            PokedexEntry entry = Database.getEntry(this.needed.getFirst());
            this.names = entry.getName();
            for (int i = 1; i < this.needed.size(); i++)
            {
                entry = Database.getEntry(this.needed.get(i));
                this.names = this.names + ", " + entry.getTranslatedName().getString();
            }
        }
        return this.sendNoTrust(trainer).append("\n").append(this.sendLegendExtra(trainer, this.names));
    }
}
