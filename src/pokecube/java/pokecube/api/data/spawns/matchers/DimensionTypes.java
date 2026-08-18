package pokecube.api.data.spawns.matchers;

import com.google.common.collect.Sets;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

import java.util.Set;

/**
 * 
 * This class matches the dimension type for the world for the location. <br>
 * <br>
 * Matcher key: "dimension_type" <br>
 * Json keys: <br>
 * "types" - String, Optional, Dimension type tags to match, separated by , <br>
 *
 */
@MatcherFunction(name = "dimension_type")
public class DimensionTypes extends BaseMatcher
{
    public String types = "";

    private Set<TagKey<DimensionType>> _validTypes = Sets.newHashSet();

    /**
     * Test if biome matches
     */
    public boolean matches(final Holder<DimensionType> type)
    {
        boolean matched = false;
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
        return matches(checker.dimensionType) ? MatchResult.SUCCEED : MatchResult.FAIL;
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
                TagKey<DimensionType> tag = TagKey.create(Registries.DIMENSION_TYPE, ResourceLocation.parse(s.replace("#", "")));
                this._validTypes.add(tag);
            }
        }
    }
}
