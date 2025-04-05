package pokecube.api.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Cancelling this event will completely prevent the default population of
 * SpawnRateMask.RATE_MASKS, thereby disabling the location dependent spawn
 * rates for specific mobs.
 *
 */
public class SpawnMaskEvent extends Event implements ICancellableEvent
{

}
