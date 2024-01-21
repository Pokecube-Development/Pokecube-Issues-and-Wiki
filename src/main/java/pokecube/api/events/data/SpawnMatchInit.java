package pokecube.api.events.data;

import net.minecraftforge.eventbus.api.Event;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.spawns.matchers.MatchChecker;

/**
 * Called after calling init for a MatchChecker. Can be used to make adjustments
 * accordingly.
 */
public class SpawnMatchInit extends Event
{
    public static MatchChecker initMatchChecker(MatchChecker toInit)
    {
        toInit.init();
        var event = new SpawnMatchInit(toInit);
        PokecubeAPI.POKEMOB_BUS.post(event);
        toInit = event.getMatchChecker();
        return toInit;
    }

    private MatchChecker checker;

    public SpawnMatchInit(MatchChecker toInit)
    {
        this.checker = toInit;
    }

    public MatchChecker getMatchChecker()
    {
        return checker;
    }

    public void setMatchChecker(MatchChecker checker)
    {
        this.checker = checker;
    }
}
