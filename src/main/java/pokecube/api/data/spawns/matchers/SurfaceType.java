package pokecube.api.data.spawns.matchers;

import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

/**
 * 
 * This class matches the block position for a location. <br>
 * <br>
 * Matcher key: "location" <br>
 * Json keys: <br>
 * "f" - String, function of x, y, z to check. Valid if the result is > 0 <br>
 *
 */
@MatcherFunction(name = "surface")
public class SurfaceType extends BaseMatcher
{
    public String type;

    boolean _caves = false;
    boolean _surface = true;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        if (_caves && _surface) return MatchResult.SUCCEED;
        int y = checker.location.getMaxY(checker.world);
        if (!_caves && checker.location.y <= y)
        {
            return MatchResult.FAIL;
        }
        if (!_surface && checker.location.y >= y)
        {
            return MatchResult.FAIL;
        }
        return MatchResult.SUCCEED;
    }

    @Override
    public void init()
    {
        if ("underground".equals(type))
        {
            _caves = true;
            _surface = false;
        }
        else if ("surface".equals(type))
        {
            _caves = false;
            _surface = true;
        }
        else if ("any".equals(type))
        {
            _caves = true;
            _surface = true;
        }
    }
}
