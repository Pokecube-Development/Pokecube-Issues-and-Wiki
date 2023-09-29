package pokecube.api.data.spawns.matchers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

/**
 * 
 * This class matches the time for a condition. <br>
 * <br>
 * Matcher key: "material" <br>
 * Json keys: <br>
 * "material" - String, Fluid or material to spawn in, default is "air" <br>
 * default times
 *
 */
@MatcherFunction(name = "material")
public class Material extends BaseMatcher
{
    public String material = "air";

    private boolean _air = true;
    private TagKey<Fluid> _tag;

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        if (_air) return checker.fluid.isEmpty() ? MatchResult.SUCCEED : MatchResult.FAIL;
        return checker.fluid.is(_tag) ? MatchResult.SUCCEED : MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        if ("air".equals(material))
        {
            _air = true;
            return;
        }
        _tag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), new ResourceLocation(material));
    }
}
