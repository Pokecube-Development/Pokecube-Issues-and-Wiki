package pokecube.api.data.pokedex.conditions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.util.JsonUtil;

public interface PokemobCondition
{
    public static Map<String, Class<? extends PokemobCondition>> CONDITIONS = new HashMap<>();

    default PokemobCondition and(PokemobCondition other)
    {
        return (mobIn) -> {
            return this.matches(mobIn) && other.matches(mobIn);
        };
    }

    default PokemobCondition not()
    {
        return (mobIn) -> !this.matches(mobIn);
    }

    boolean matches(IPokemob mobIn);

    default void init()
    {}

    public static PokemobCondition makeFromElement(JsonElement element)
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

    public static PokemobCondition makeFromArray(JsonArray array)
    {
        PokemobCondition root = null;
        for (int i = 0; i < array.size(); i++)
        {
            JsonElement e = array.get(i);
            var made = makeFromElement(e);
            if (root == null) root = made;
            else if (made != null) root = root.and(made);
        }
        return root;
    }

    public static PokemobCondition makeFromObject(JsonObject obj)
    {
        if (!obj.has("key"))
        {
            PokecubeAPI.LOGGER.error("missing key {} for a mega evo rule!", obj);
            return null;
        }
        String key = obj.get("key").getAsString();
        Class<? extends PokemobCondition> condClass = CONDITIONS.get(key);
        if (condClass == null)
        {
            PokecubeAPI.LOGGER.error("invalid type key {} for a mega evo rule!", key);
            return null;
        }
        PokemobCondition condition = JsonUtil.gson.fromJson(obj, condClass);
        condition.init();
        return condition;
    }
}
