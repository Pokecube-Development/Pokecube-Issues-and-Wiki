package pokecube.core.events.onload;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.eventbus.api.Event;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;

/**
 * This event is fired during the item registration phase. Add any pokecubes
 * you want to register here. The name of the cube will be <prefix_>cube
 */
public class RegisterPokecubes extends Event
{
    public final List<PokecubeBehavior> behaviors = Lists.newArrayList();

    public RegisterPokecubes()
    {
        PokecubeCore.LOGGER.debug("Cube Registry Event");
    }

    public void register(final PokecubeBehavior behaviour)
    {
        PokecubeCore.LOGGER.debug("Registering cube: " + behaviour.getRegistryName());
        this.behaviors.add(behaviour);
    }
}
