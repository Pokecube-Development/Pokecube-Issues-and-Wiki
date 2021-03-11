package pokecube.legends.conditions;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.util.text.IFormattableTextComponent;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public abstract class AbstractEntriedCondition extends AbstractCondition
{
    final List<String> needed;

    String names;

    public AbstractEntriedCondition(final String... needed)
    {
        this.needed = Lists.newArrayList(needed);
    }

    @Override
    boolean hasRequirements(final Entity trainer)
    {
        for (final String s : this.needed)
            if (this.caughtNumber(trainer, Database.getEntry(s)) <= 0) return false;
        return true;
    }

    @Override
    public IFormattableTextComponent getFailureMessage(final Entity trainer)
    {
        if (this.names == null)
        {
            this.names = "";
            PokedexEntry entry = Database.getEntry(this.needed.get(0));
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
