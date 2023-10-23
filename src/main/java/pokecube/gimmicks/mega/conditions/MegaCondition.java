package pokecube.gimmicks.mega.conditions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.util.JsonUtil;

public interface MegaCondition
{
    public static Map<String, Class<? extends MegaCondition>> CONDITIONS = new HashMap<>();

    public static MegaCondition makeFromElement(JsonElement element)
    {
        if (element.isJsonArray())
        {
            var arr = element.getAsJsonArray();
            return makeFromArray(arr);
        }
        else if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            return makeFromObject(obj);
        }
        return null;
    }

    public static MegaCondition makeFromArray(JsonArray array)
    {
        MegaCondition root = null;
        for (int i = 0; i < array.size(); i++)
        {
            JsonElement e = array.get(i);
            var made = makeFromElement(e);
            if (root == null) root = made;
            else if (made != null) root = root.and(made);
        }
        return root;
    }
    
    public static MegaCondition makeFromObject(JsonObject obj)
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
        condition.init();
        return condition;
    }
    
    default MegaCondition and(MegaCondition other)
    {
        return (mobIn, entryTo) -> {
            return this.matches(mobIn, entryTo) && other.matches(mobIn, entryTo);
        };
    }

    boolean matches(IPokemob mobIn, PokedexEntry entryTo);

    default void init()
    {}
}
