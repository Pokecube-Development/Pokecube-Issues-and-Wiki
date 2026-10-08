package pokecube.gimmicks.mega.conditions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.HolderLookup;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.util.JsonUtil;

public interface MegaCondition
{
    public static Map<String, Class<? extends MegaCondition>> CONDITIONS = new HashMap<>();

    public static MegaCondition makeFromElement(HolderLookup.Provider registries, JsonElement element)
    {
        if (element.isJsonArray())
        {
            var arr = element.getAsJsonArray();
            return makeFromArray(registries, arr);
        }
        else if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            return makeFromObject(registries, obj);
        }
        return null;
    }

    public static MegaCondition makeFromArray(HolderLookup.Provider registries, JsonArray array)
    {
        MegaCondition root = null;
        for (int i = 0; i < array.size(); i++)
        {
            JsonElement e = array.get(i);
            var made = makeFromElement(registries, e);
            if (root == null) root = made;
            else if (made != null) root = root.and(made);
        }
        return root;
    }

    public static MegaCondition makeFromObject(HolderLookup.Provider registries, JsonObject obj)
    {
        if (!obj.has("key"))
        {
            PokecubeAPI.LOGGER.error("missing key {} for a mega evo rule!", obj);
            return null;
        }
        String key = obj.get("key").getAsString();
        Class<? extends MegaCondition> condClass = CONDITIONS.get(key);
        if (condClass == null)
        {
            PokecubeAPI.LOGGER.error("invalid type key {} for a mega evo rule!", key);
            return null;
        }
        MegaCondition condition = JsonUtil.gson.fromJson(obj, condClass);
        condition.init(registries);
        return condition;
    }

    default MegaCondition and(MegaCondition other)
    {
        return (mobIn, entryTo) -> this.matches(mobIn, entryTo) && other.matches(mobIn, entryTo);
    }

    boolean matches(IPokemob mobIn, PokedexEntry entryTo);

    default void init(HolderLookup.Provider registries)
    {}
}
