package pokecube.api.data.spawns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.spawns.matchers.MatchChecker;
import pokecube.api.data.spawns.matchers.MatcherLoaders;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import thut.api.util.JsonUtil;
import thut.lib.TComponent;

public class SpawnRule
{
    public static Set<String> NOT_LOCATION_VALUES = new HashSet<>();

    static
    {
        NOT_LOCATION_VALUES.add("min");
        NOT_LOCATION_VALUES.add("max");
        NOT_LOCATION_VALUES.add("rate");
        NOT_LOCATION_VALUES.add("level");
        NOT_LOCATION_VALUES.add("variance");
        NOT_LOCATION_VALUES.add("minY");
        NOT_LOCATION_VALUES.add("maxY");
    }

    public String preset = "";
    public String and_preset = "";
    public String not_preset = "";
    public String or_preset = "";
    public String desc = "";
    public Map<String, Object> matchers = Maps.newHashMap();
    public Map<String, Object> values = Maps.newHashMap();

    public DefaultFormeHolder model = null;

    public JsonObject biomes = null;

    public List<MatchChecker> _matchers = new ArrayList<>();

    private Component _cached_desc = null;

    private String __cache__ = null;

    @Override
    public String toString()
    {
        if (__cache__ != null) return __cache__;
        return __cache__ = JsonUtil.smol_gson.toJson(this);
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

    public void loadMatchers()
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
        boolean hasPresets = !(preset.isBlank() && and_preset.isBlank() && or_preset.isBlank() && not_preset.isBlank());
        if (hasPresets) return true;
        int extras = 0;
        for (String s : Lists.newArrayList(this.values.keySet()))
        {
            Object o = this.values.get(s);
            if (o == null || (o instanceof String s2 && s2.isBlank()) || (o instanceof Collection<?> c && c.isEmpty()))
            {
                this.values.remove(s);
            }
            if (NOT_LOCATION_VALUES.contains(s)) extras++;
        }
        return this.values.size() > extras || this.biomes != null || !matchers.isEmpty();
    }

    public SpawnRule copy()
    {
        SpawnRule copy = JsonUtil.gson.fromJson(JsonUtil.gson.toJson(this), getClass());
        return copy;
    }

    @Nullable
    public Component makeDescription()
    {
        if (_cached_desc != null) return _cached_desc;
        if (this.desc != null && !this.desc.isBlank()) _cached_desc = TComponent.translatable(desc);
        if (_cached_desc == null)
        {
            if (this._matchers.isEmpty() && !this.matchers.isEmpty())
            {
                this.loadMatchers();
            }
            // We should try to somehow merge together internal ones.
            for (var m : this._matchers)
            {
                var desc = m.makeDescription();
                // For now test just returning the first though.
                if (desc != null) return _cached_desc = desc;
            }
        }
        return _cached_desc;
    }
}
