package pokecube.api.data.spawns.matchers;

import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.api.data.spawns.SpawnCheck.Weather;

/**
 * 
 * This class matches the global weather. <br>
 * <br>
 * Matcher key: "weather" <br>
 * Json keys: <br>
 * "type" - String, one of: "sun", "cloud", "rain", "snow", "none" <br>
 * <br>
 * "none" is the response if not above ground.
 *
 */
@MatcherFunction(name = "weather")
public class WeatherMatch extends BaseMatcher
{
    public String type = "";

    private Weather _weather;
    private boolean _thunder = false;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        if (_thunder) return checker.thundering ? MatchResult.SUCCEED : MatchResult.FAIL;
        return checker.weather == _weather ? MatchResult.SUCCEED : MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        this._weather = getWeather(type);
        _thunder = false;
        if ("thunder".equals(type)) _thunder = true;
    }

    private Weather getWeather(final String name)
    {
        for (final Weather c : Weather.values()) if (c.name().equalsIgnoreCase(name)) return c;
        return Weather.NONE;
    }

}
