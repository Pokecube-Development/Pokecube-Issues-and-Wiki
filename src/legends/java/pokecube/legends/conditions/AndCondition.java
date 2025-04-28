package pokecube.legends.conditions;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.legends.conditions.data.Conditions;

import java.util.List;

public class AndCondition extends AbstractCondition
{
    List<AbstractCondition> list;

    public AndCondition(List<AbstractCondition> list)
    {
        this.list = list;
    }

    @Override
    public void setEntry(PokedexEntry entry)
    {
        super.setEntry(entry);
        list.forEach(e -> e.setEntry(entry));
    }

    @Override
    public void setSpawnRule(Conditions.Spawn spawn)
    {
        super.setSpawnRule(spawn);
        list.forEach(e -> e.setSpawnRule(spawn));
    }

    @Override
    public CanSpawn canSpawn(SpawnEvent.SpawnContext context, boolean message)
    {
        CanSpawn test = super.canSpawn(context, message);
        if (!test.test()) return test;
        for (var entry : list)
        {
            test = entry.canSpawn(context, message);
            if (!test.test()) return test;
        }
        return CanSpawn.NO;
    }

    @Override
    public CanSpawn canSpawn(SpawnEvent.SpawnContext context)
    {
        CanSpawn test = super.canSpawn(context);
        if (!test.test()) return test;
        for (var entry : list)
        {
            test = entry.canSpawn(context);
            if (!test.test()) return test;
        }
        return CanSpawn.YES;
    }

    @Override
    protected boolean hasRequirements(final Entity trainer)
    {
        return list.stream().allMatch(entry -> entry.hasRequirements(trainer));
    }

    @Override
    public MutableComponent getFailureMessage(Entity trainer)
    {
        if (this.customFailMesg != null) return super.getFailureMessage(trainer);
        MutableComponent component = null;
        for (var entry : this.list)
        {
            var failed = !entry.hasRequirements(trainer);
            if (failed)
            {
                if (component == null) component = entry.getFailureMessage(trainer);
                else component = component.append("\n").append(entry.getFailureMessage(trainer));
            }
        }
        return component;
    }
}
