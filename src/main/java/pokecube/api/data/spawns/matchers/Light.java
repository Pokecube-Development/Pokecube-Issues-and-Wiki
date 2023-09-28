package pokecube.api.data.spawns.matchers;

import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

/**
 * 
 * This class matches the block light for a location. <br>
 * <br>
 * Matcher key: "light" <br>
 * Json keys: <br>
 * "min" - float, minimum light to match <br>
 * "max" - float, maximum light to match
 *
 */
@MatcherFunction(name = "light")
public class Light extends BaseMatcher
{
    public float min;
    public float max;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        return checker.light <= max && checker.light >= min ? MatchResult.SUCCEED : MatchResult.FAIL;
    }
}
