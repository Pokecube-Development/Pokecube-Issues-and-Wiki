package pokecube.api.data.spawns;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.spawns.matchers.MatchChecker;
import pokecube.api.data.spawns.matchers.MatcherLoaders;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import thut.api.util.JsonUtil;

public class SpawnRule
{

    public String preset = "";
    public String and_preset = "";
    public String not_preset = "";
    public String or_preset = "";
    public Map<String, Object> matchers = Maps.newHashMap();
    public Map<String, Object> values = Maps.newHashMap();

    public DefaultFormeHolder model = null;

    public JsonObject biomes = null;

    private String __cache__ = null;

    @Override
    public String toString()
    {
        if (__cache__ != null) return __cache__;
        return __cache__ = JsonUtil.gson.toJson(this);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof SpawnRule other)) return false;
        return other.toString().equals(this.toString());
    }

    public FormeHolder getForme(final PokedexEntry baseEntry)
    {
        if (this.model != null) return this.model.getForme(baseEntry);
        return null;
    }

    public String getString(String key)
    {
        Object o = this.values.get(key);
        if (o instanceof String s) return s;
        return null;
    }

    public String removeString(String key)
    {
        Object o = this.values.remove(key);
        if (o instanceof String s) return s;
        if (o != null) this.values.put(key, o);
        return null;
    }

    public void initMatchers()
    {
        this.matchers.replaceAll((key, value) -> {
            // Leave the match checkers alone after further runs of this
            if (value instanceof MatchChecker) return value;

            // Now look up classes
            Class<?> clazz = MatcherLoaders.matchClasses.get(key);

            // And convert from strings as needed
            if (clazz != null)
            {
                String json = JsonUtil.gson.toJson(value);
                return JsonUtil.gson.fromJson(json, clazz);
            }
            return null;
        });
    }

    public boolean isValid()
    {
        if (!this.preset.isBlank()) this.values.put(SpawnBiomeMatcher.PRESET, this.preset);
        if (!this.and_preset.isBlank()) this.values.put(SpawnBiomeMatcher.ANDPRESET, this.and_preset);
        if (!this.or_preset.isBlank()) this.values.put(SpawnBiomeMatcher.ORPRESET, this.or_preset);
        if (!this.not_preset.isBlank()) this.values.put(SpawnBiomeMatcher.NOTPRESET, this.not_preset);
        for (String s : Lists.newArrayList(this.values.keySet()))
        {
            Object o = this.values.get(s);
            if (o == null || (o instanceof String s2 && s2.isBlank()) || (o instanceof Collection<?> c && c.isEmpty()))
                this.values.remove(s);
        }
        return !this.values.isEmpty() || this.biomes != null || !matchers.isEmpty();
    }

    public SpawnRule copy()
    {
        SpawnRule copy = JsonUtil.gson.fromJson(JsonUtil.gson.toJson(this), getClass());
        return copy;
    }
}
