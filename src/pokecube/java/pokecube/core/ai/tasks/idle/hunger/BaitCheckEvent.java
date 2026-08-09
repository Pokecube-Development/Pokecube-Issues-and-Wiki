package pokecube.core.ai.tasks.idle.hunger;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.tasks.idle.HungerTask;

/**
 * This is fired on the PokecubeAPI.POKEMOB_BUS
 * <p>
 * Cancelling this event will prevent regular bait check from running.
 */
public class BaitCheckEvent extends Event implements ICancellableEvent
{
    public final IPokemob pokemob;
    public final HungerTask task;

    public BaitCheckEvent(IPokemob pokemob, HungerTask task)
    {
        this.pokemob = pokemob;
        this.task = task;
    }
}
