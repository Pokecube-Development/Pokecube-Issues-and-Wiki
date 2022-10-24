package pokecube.api.data.spawns;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import thut.api.util.JsonUtil;

public class SpawnRule
{

    public String preset = "";
    public String and_preset = "";
    public String not_preset = "";
    public String or_preset = "";
    public Map<String, String> values = Maps.newHashMap();

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

    public boolean isValid()
    {
        if (!this.preset.isBlank()) this.values.put(SpawnBiomeMatcher.PRESET, this.preset);
        if (!this.and_preset.isBlank()) this.values.put(SpawnBiomeMatcher.ANDPRESET, this.and_preset);
        if (!this.or_preset.isBlank()) this.values.put(SpawnBiomeMatcher.ORPRESET, this.or_preset);
        if (!this.not_preset.isBlank()) this.values.put(SpawnBiomeMatcher.NOTPRESET, this.not_preset);
        for (String s : Lists.newArrayList(this.values.keySet()))
            if (this.values.get(s).isBlank()) this.values.remove(s);
        return !this.values.isEmpty() || this.biomes != null;
    }

    public SpawnRule copy()
    {
        SpawnRule copy = JsonUtil.gson.fromJson(JsonUtil.gson.toJson(this), getClass());
        return copy;
    }
}
