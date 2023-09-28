package pokecube.api.data.spawns.matchers;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.core.utils.TimePeriod;

@MatcherFunction(name = "time")
public class Time extends BaseMatcher
{
    public float start = 0;
    public float end = 0;
    public String preset = "";

    private TimePeriod _time;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        return _time.contains(checker.time) ? MatchResult.SUCCEED : MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        _time = null;
        if (!preset.isEmpty())
        {
            switch (preset)
            {
            case "day":
                _time = PokedexEntry.day;
                break;
            case "dusk":
                _time = PokedexEntry.dusk;
                break;
            case "dawn":
                _time = PokedexEntry.dawn;
                break;
            case "night":
                _time = PokedexEntry.night;
                break;
            }
        }
        if (_time == null) _time = new TimePeriod(start, end);
    }
}
