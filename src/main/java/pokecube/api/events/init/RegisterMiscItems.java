package pokecube.api.events.init;

import net.minecraftforge.eventbus.api.Event;

/**
 * This event is called before generating the items, During this event, you
 * should do the following via pokecube.core.init.ItemGenerator:
 * 
 * Register berries <br>
 * Register custom held items<br>
 * <BR>
 * 
 * Items registered this way will all be prefixed as "pokecube", and will be
 * registered later.
 **/
public class RegisterMiscItems extends Event
{

}
