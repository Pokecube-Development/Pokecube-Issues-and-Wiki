package pokecube.api.events.pokemobs;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.entity.pokemob.IPokemob;

/** Fired on the PokecubeAPI.POKEMOB_BUS */
public class RecallEvent extends Event implements ICancellableEvent
{
    /**
     * fired before any other logic is done, this should be used if you want to
     * completely cancel recalling, and do no other processing
     */
    public static class Pre extends RecallEvent implements ICancellableEvent
    {
        public Pre(final IPokemob pokemob)
        {
            super(pokemob);
        }
    }
    public static class Post extends RecallEvent
    {
        public Post(final IPokemob pokemob)
        {
            super(pokemob);
        }
    }

    public final IPokemob recalled;

    protected RecallEvent(final IPokemob pokemob)
    {
        this.recalled = pokemob;
    }
}
