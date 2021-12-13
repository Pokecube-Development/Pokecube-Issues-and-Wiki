package pokecube.core.events;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
/**
 * Cancelling this event will completely prevent the default population of
 * SpawnRateMask.RATE_MASKS, thereby disabling the location dependent spawn
 * rates for specific mobs.
 *
 */
public class SpawnMaskEvent extends Event
{

}
