package pokecube.api.events.data;

import net.minecraftforge.eventbus.api.Event;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.pokedex.conditions.PokemobCondition;

/**
 * Called after calling init for a PokemobCondition. Can be used to make
 * adjustments accordingly.
 */
public class PokemobMatchInit extends Event
{
    public static PokemobCondition initMatchChecker(PokemobCondition toInit)
    {
        toInit.init();
        var event = new PokemobMatchInit(toInit);
        PokecubeAPI.POKEMOB_BUS.post(event);
        toInit = event.getMatchChecker();
        return toInit;
    }

    private PokemobCondition checker;

    public PokemobMatchInit(PokemobCondition toInit)
    {
        this.checker = toInit;
    }

    public PokemobCondition getMatchChecker()
    {
        return checker;
    }

    public void setMatchChecker(PokemobCondition checker)
    {
        this.checker = checker;
    }
}
