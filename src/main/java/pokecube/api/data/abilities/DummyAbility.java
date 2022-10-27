package pokecube.api.data.abilities;

import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;

public class DummyAbility extends Ability
{
    @Override
    public Ability setName(String name)
    {
        if (PokecubeCore.getConfig().debug_data)
            PokecubeAPI.LOGGER.warn("Warning, ability named {} is un-registered", name);
        return super.setName(name);
    }
}
