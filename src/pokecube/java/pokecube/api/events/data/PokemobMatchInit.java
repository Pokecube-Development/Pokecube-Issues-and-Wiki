package pokecube.api.events.data;

import net.minecraft.core.HolderLookup;
import net.neoforged.bus.api.Event;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.pokedex.conditions.PokemobCondition;

import java.util.List;

/**
 * Called after calling init for a PokemobCondition. Can be used to make
 * adjustments accordingly.
 */
public class PokemobMatchInit extends Event
{
    public static PokemobCondition initMatchChecker(HolderLookup.Provider registries, PokemobCondition toInit,
            List<PokemobCondition> bits)
    {
        toInit.init(registries);
        var event = new PokemobMatchInit(toInit);
        PokecubeAPI.POKEMOB_BUS.post(event);
        toInit = event.getMatchChecker();
        bits.add(toInit);
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
