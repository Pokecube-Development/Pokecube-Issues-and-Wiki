package pokecube.legends.conditions;

import net.minecraft.world.entity.Entity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.legends.conditions.data.Conditions;

import java.util.List;

public class OrCondition extends AbstractCondition
{
    List<AbstractCondition> list;

    public OrCondition(List<AbstractCondition> list)
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
            if (test.test()) return test;
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
            if (test.test()) return test;
        }
        return CanSpawn.NO;
    }

    @Override
    protected boolean hasRequirements(final Entity trainer)
    {
        return list.stream().anyMatch(entry -> entry.hasRequirements(trainer));
    }
}
