package pokecube.api.events.pokemobs;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.entity.pokemob.IPokemob;

@Cancelable
/** Fired on the PokecubeAPI.POKEMOB_BUS */
public class RecallEvent extends Event
{
    @Cancelable
    /**
     * fired before any other logic is done, this should be used if you want to
     * completely cancel recalling, and do no other processing
     */
    public static class Pre extends RecallEvent
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
