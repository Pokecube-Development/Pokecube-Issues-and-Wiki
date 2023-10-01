package pokecube.api.data.spawns.matchers;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.core.database.Database;
import thut.api.level.terrain.BiomeDatabase;
import thut.api.level.terrain.BiomeType;

/**
 * 
 * This class matches the biome or subbiome for a location. <br>
 * <br>
 * Matcher key: "biome" <br>
 * Json keys: <br>
 * "biome_tags" - String, Optional, Biome tags to match, separated by , <br>
 * "sub_biomes" - String, Optional, Subbiomes to match, separated by , <br>
 *
 */
@MatcherFunction(name = "biome")
public class Biomes extends BaseMatcher
{
    public String biome_tags = "";
    public String sub_biomes = "";

    private Set<TagKey<Biome>> _validBiomes = Sets.newHashSet();
    private Set<BiomeType> _validSubBiomes = Sets.newHashSet();
    private boolean _allValid = false;

    /**
     * Test if biome matches
     */
    public boolean matches(final Holder<Biome> biome)
    {
        boolean matched = true;
        for (var tag : _validBiomes)
        {
            matched = matched && biome.is(tag);
        }
        return negate ? !matched : matched;
    }

    /**
     * Test if subbiome matches
     */
    public boolean matches(final BiomeType biome)
    {
        boolean matched = true;
        if (!_allValid) for (var subbiome : _validSubBiomes)
        {
            matched = matched && biome.equals(subbiome);
        }
        return negate ? !matched : matched;
    }

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        boolean biomeValid = matches(checker.biome);
        boolean subbiomeValid = matches(checker.type);

        return biomeValid && subbiomeValid ? MatchResult.SUCCEED : MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        _validBiomes.clear();
        _validSubBiomes.clear();
        if (!biome_tags.isBlank())
        {
            String[] args = biome_tags.split(",");
            for (String s : args)
            {
                s = s.strip();
                if (BiomeDatabase.isBiomeTag(s))
                {
                    TagKey<Biome> tag = TagKey.create(Registry.BIOME_REGISTRY,
                            new ResourceLocation(s.replace("#", "")));
                    this._validBiomes.add(tag);
                    continue;
                }
                else
                {
                    PokecubeAPI.LOGGER.error("Warning, not a valid biome tag name: {} in {}", s, biome_tags);
                }
            }
        }
        _allValid = false;
        if (!sub_biomes.isBlank())
        {
            String[] args = sub_biomes.split(",");
            for (String s : args)
            {
                s = Database.trim(s);
                final BiomeType subBiome = BiomeType.getBiome(s);
                this._validSubBiomes.add(subBiome);
                if (subBiome == BiomeType.ALL) _allValid = true;
            }
        }
    }
}
