package pokecube.legends.conditions;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEntriedCondition extends AbstractCondition
{
    public final List<String> needed;

    MutableComponent names;

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
            this.names = Component.literal("[");
            List<PokedexEntry> needed = new ArrayList<>();
            this.needed.forEach(name -> needed.add(Database.getEntry(name)));
            names = names.append(Component.translatable(needed.removeFirst().getUnlocalizedName()));
            while (!needed.isEmpty())
                names = names.append(", ").append(Component.translatable(needed.removeFirst().getUnlocalizedName()));
            names = names.append("]");
        }
        return this.sendNoTrust(trainer).append("\n").append(this.sendLegendExtra(this.names));
    }
}
