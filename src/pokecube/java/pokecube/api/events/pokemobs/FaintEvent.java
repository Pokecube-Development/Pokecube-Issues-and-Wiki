package pokecube.api.events.pokemobs;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.TriState;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * Fired on the PokecubeAPI.POKEMOB_BUS
 * <br>
 * Result Cases:
 * DENY - this will prevent the mob from recalling after fainting
 * ALLOW - this will force the mob to recall after fainting
 * DEFAULT - this will follow whatever the configs say for this mob
 */
public class FaintEvent extends Event
{
    public final IPokemob pokemob;

    public FaintEvent(final IPokemob pokemob)
    {
        this.pokemob = pokemob;
    }

    private TriState result = TriState.DEFAULT;

    public void setResult(TriState result)
    {
        this.result = result;
    }

    public TriState getResult()
    {
        return this.result;
    }

}
