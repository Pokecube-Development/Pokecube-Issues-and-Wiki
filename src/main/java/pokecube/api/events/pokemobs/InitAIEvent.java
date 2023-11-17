package pokecube.api.events.pokemobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.event.entity.EntityEvent;
import pokecube.api.ai.ITaskAdder;
import pokecube.api.ai.TaskAdders;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.entity.ai.IAIRunnable;

/**
 * Called during initiating of the pokemob's AI. fired on the
 * PokecubeAPI.POKEMOB_BUS
 */
public class InitAIEvent extends EntityEvent
{
    private final IPokemob pokemob;

    protected InitAIEvent(final IPokemob entity)
    {
        super(entity.getEntity());
        this.pokemob = entity;
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }

    /**
     * This event is called after the brain has been initialized, it is for any
     * final adjustments that need to be done.
     */
    public static class Post extends InitAIEvent
    {
        public Post(final IPokemob entity)
        {
            super(entity);
        }

    }

    public static class Init extends InitAIEvent
    {
        public static enum Type
        {
            IDLE, COMBAT, UTILITY;
        }

        public final Type type;

        public final List<IAIRunnable> tasks;
        public final Map<String, IAIRunnable> namedTasks;

        public Init(final IPokemob entity, final Type type, final List<IAIRunnable> tasks)
        {
            super(entity);
            this.type = type;
            this.tasks = tasks;
            namedTasks = new HashMap<>();
            for (var task : this.tasks) if (!task.getIdentifier().isBlank()) namedTasks.put(task.getIdentifier(), task);
            for (final ITaskAdder adder : TaskAdders.getAdders(type)) adder.addTasks(entity, tasks, namedTasks);
            // Init again after ask adders, incase they forget to update..
            namedTasks.clear();
            for (var task : this.tasks) if (!task.getIdentifier().isBlank()) namedTasks.put(task.getIdentifier(), task);
        }

        public void add(IAIRunnable task)
        {
            tasks.add(task);
            if (!task.getIdentifier().isBlank()) namedTasks.put(task.getIdentifier(), task);
        }

    }
}
