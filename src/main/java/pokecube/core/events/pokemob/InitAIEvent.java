package pokecube.core.events.pokemob;

import net.minecraftforge.event.entity.EntityEvent;
import pokecube.core.interfaces.IPokemob;

/**
 * Called after initiating the pokemob's AI. fired on the
 * PokecubeCore.POKEMOB_BUS
 */
public class InitAIEvent extends EntityEvent
{
    private final IPokemob pokemob;

    public InitAIEvent(IPokemob entity)
    {
        super(entity.getEntity());
        this.pokemob = entity;
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }
}
