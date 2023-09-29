package pokecube.api.data.spawns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public List<MatchChecker> _matchers = new ArrayList<>();

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
        this._matchers.clear();
        this.matchers.forEach((key, value) -> {
            // Leave the match checkers alone after further runs of this
            if (value instanceof MatchChecker match)
            {
                this._matchers.add(match);
                return;
            }

            // Now look up classes
            Class<? extends MatchChecker> clazz = MatcherLoaders.matchClasses.get(key);

            // If it is a list, load for each in list
            if (value instanceof List<?> list)
            {
                list.forEach(value2 -> {
                    String json = JsonUtil.gson.toJson(value2);
                    this._matchers.add(JsonUtil.gson.fromJson(json, clazz));
                });
            }
            // And convert from strings as needed
            else if (clazz != null)
            {
                String json = JsonUtil.gson.toJson(value);
                this._matchers.add(JsonUtil.gson.fromJson(json, clazz));
            }
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
