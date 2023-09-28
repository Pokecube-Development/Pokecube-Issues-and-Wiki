package pokecube.api.data.spawns.matchers;

import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

@MatcherFunction(name = "light")
public class Light extends BaseMatcher
{
    public float min;
    public float max;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        return checker.light < max && checker.light >= min ? MatchResult.SUCCEED : MatchResult.FAIL;
    }
}
