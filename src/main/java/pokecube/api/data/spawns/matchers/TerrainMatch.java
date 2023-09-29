package pokecube.api.data.spawns.matchers;

import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.api.data.spawns.SpawnCheck.TerrainType;

/**
 * 
 * This class matches the local terrain. <br>
 * <br>
 * Matcher key: "terrain" <br>
 * Json keys: <br>
 * "type" - String, one of: "flat", "hills" <br>
 * <br>
 * The response is based on the local gradient of the land.
 *
 */
@MatcherFunction(name = "terrain")
public class TerrainMatch extends BaseMatcher
{
    public String type = "";

    private TerrainType _terrain;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        return checker.terrain == _terrain ? MatchResult.SUCCEED : MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        this._terrain = getTerrain(type);
    }

    private TerrainType getTerrain(final String name)
    {
        for (final TerrainType c : TerrainType.values()) if (c.name().equalsIgnoreCase(name)) return c;
        return TerrainType.FLAT;
    }

}
