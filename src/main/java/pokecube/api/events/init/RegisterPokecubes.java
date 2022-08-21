package pokecube.api.events.init;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.eventbus.api.Event;
import pokecube.api.PokecubeAPI;
import pokecube.api.items.IPokecube.PokecubeBehaviour;

/**
 * This event is fired during the item registration phase. Add any pokecubes
 * you want to register here. The name of the cube will be &lt;prefix_&gt;cube
 */
public class RegisterPokecubes extends Event
{
    public final List<PokecubeBehaviour> behaviors = Lists.newArrayList();

    public RegisterPokecubes()
    {
        PokecubeAPI.LOGGER.debug("Cube Registry Event");
    }

    public void register(final PokecubeBehaviour behaviour)
    {
        PokecubeAPI.LOGGER.debug("Registering cube: " + behaviour.name);
        this.behaviors.add(behaviour);
    }
}
