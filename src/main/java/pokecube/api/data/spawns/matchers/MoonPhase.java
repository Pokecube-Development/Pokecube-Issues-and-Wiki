package pokecube.api.data.spawns.matchers;

import net.minecraft.network.chat.Component;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
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
@MatcherFunction(name = "moon_phase")
public class MoonPhase extends BaseMatcher
{
    public int phase;
    public float max = 1;
    public float min = 0;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        int moon_time = checker.world.getMoonPhase();
        if (phase >= 0 && moon_time != phase) return MatchResult.FAIL;
        float brightness = checker.world.getMoonBrightness();
        return brightness >= min && brightness <= max ? MatchResult.SUCCEED : MatchResult.FAIL;
    }

    @Override
    public Component makeDescription()
    {
        return TComponent.translatable("pokemob.description.evolve.moon_phase_" + phase);
    }
}
