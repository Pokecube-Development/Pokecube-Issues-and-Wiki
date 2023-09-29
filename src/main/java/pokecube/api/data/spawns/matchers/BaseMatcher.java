package pokecube.api.data.spawns.matchers;

import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

/**
 * Base matcher class, includes the json key: "negate", which inverts the
 * condition.
 *
 */
public abstract class BaseMatcher implements MatchChecker
{
    public boolean negate = false;

    public abstract MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker);

    @Override
    public final MatchResult matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        MatchResult base = _matches(matcher, checker);
        // If we are set to negate we swap fail and succeed, otherwise we return
        // whatever the base was.
        MatchResult result = negate ? base == MatchResult.SUCCEED ? MatchResult.FAIL : MatchResult.SUCCEED : base;
        return result;
    }
}
