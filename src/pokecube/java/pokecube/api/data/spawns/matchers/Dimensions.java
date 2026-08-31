package pokecube.api.data.spawns.matchers;

import com.google.common.collect.Sets;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

import java.util.Set;

/**
 * 
 * This class matches the dimension for the world for the location. <br>
 * <br>
 * Matcher key: "dimension" <br>
 * Json keys: <br>
 * "types" - String, Optional, Dimension tags to match, separated by , <br>
 *
 */
@MatcherFunction(name = "dimension")
public class Dimensions extends BaseMatcher
{
    public String types = "";

    private final Set<TagKey<Level>> _validTypes = Sets.newHashSet();

    /**
     * Test if biome matches
     */
    public boolean matches(final Holder<Level> type)
    {
        boolean matched = true;
        for (var tag : _validTypes)
        {
            matched = matched || type.is(tag);
            if(matched) break;
        }
        return negate != matched;
    }

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        return matches(checker.dimension) ? MatchResult.SUCCEED : MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        _validTypes.clear();
        if (!types.isBlank())
        {
            String[] args = types.split(",");
            for (String s : args)
            {
                s = s.strip();
                TagKey<Level> tag = TagKey.create(Registries.DIMENSION, ResourceLocation.parse(s.replace("#", "")));
                this._validTypes.add(tag);
            }
        }
    }
}
