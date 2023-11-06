package pokecube.api.data.spawns.matchers;

import net.minecraft.network.chat.Component;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import thut.lib.TComponent;

/**
 * 
 * This class matches the phas of the moon for a condition. <br>
 * <br>
 * Matcher key: "moon_phase" <br>
 * Json keys: <br>
 * "phase" - int, Phase of the moon, (0-7), 0 is full moon <br>
 * "max" - float, max brightness of moon, 1.0 is full, 0.0 is new <br>
 * "min" - float, min brightness of moon, 1.0 is full, 0.0 is new <br>
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
