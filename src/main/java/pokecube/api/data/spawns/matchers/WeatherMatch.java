package pokecube.api.data.spawns.matchers;

import net.minecraft.world.level.Level;
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
 * "inside" - boolean, default false. Whether it checks for weather other than "none" while underground as well<br>
 * "outside" - boolean, default true. Whether it includes the check for weather which includes "none"
 * <br>
 * "none" is the response if not above ground.
 *
 */
@MatcherFunction(name = "weather")
public class WeatherMatch extends BaseMatcher
{
    public String type = "";
    public boolean outside = true;
    public boolean inside = false;

    private Weather _weather;
    private boolean _thunder = false;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        var weather = checker.weather;
        var resp = MatchResult.FAIL;
        if (outside)
        {
            if (_thunder) resp = checker.thundering ? MatchResult.SUCCEED : MatchResult.FAIL;
            else resp = weather == _weather ? MatchResult.SUCCEED : MatchResult.FAIL;
            if (resp == MatchResult.SUCCEED) return resp;
        }
        weather = checker.outsideWeather;
        if (inside)
        {
            if (_thunder && checker.world instanceof Level level)
                resp = level.isThundering() ? MatchResult.SUCCEED : MatchResult.FAIL;
            else resp = weather == _weather ? MatchResult.SUCCEED : MatchResult.FAIL;
            if (resp == MatchResult.SUCCEED) return resp;
        }
        return resp;
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
