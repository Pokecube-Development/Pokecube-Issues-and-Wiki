package pokecube.core.events.pokemob;

import java.util.List;

import net.minecraftforge.event.entity.EntityEvent;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.IAIRunnable;

/**
 * Called during initiating of the pokemob's AI. fired on the
 * PokecubeCore.POKEMOB_BUS
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

        public Init(final IPokemob entity, final Type type, final List<IAIRunnable> tasks)
        {
            super(entity);
            this.type = type;
            this.tasks = tasks;
        }

    }
}
