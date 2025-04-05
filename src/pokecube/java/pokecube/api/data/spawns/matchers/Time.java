package pokecube.api.data.spawns.matchers;

import net.minecraft.network.chat.Component;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.core.utils.TimePeriod;
import thut.lib.TComponent;

/**
 * 
 * This class matches the time for a condition. <br>
 * <br>
 * Matcher key: "light" <br>
 * Json keys: <br>
 * "start" - float, start time of day (fraction of day, 0-1) <br>
 * "end" - float, end time of day (fraction of day, 0-1) <br>
 * "preset" - String, Optional, "day", "dawn", "dusk" or "night" to apply the
 * default times
 *
 */
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

    @Override
    public Component makeDescription()
    {
        if (preset != null && !preset.isBlank())
        {
            return TComponent.translatable("pokemob.description.evolve." + preset);
        }
        return super.makeDescription();
    }
}
