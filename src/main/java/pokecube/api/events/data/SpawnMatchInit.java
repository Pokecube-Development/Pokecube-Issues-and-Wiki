package pokecube.api.events.data;

import net.minecraftforge.eventbus.api.Event;
import pokecube.api.data.spawns.matchers.MatchChecker;

/**
 * Called after calling init for a MatchChecker. Can be used to make adjustments
 * accordingly.
 */
public class SpawnMatchInit extends Event
{
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
