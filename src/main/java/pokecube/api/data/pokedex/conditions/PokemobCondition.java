package pokecube.api.data.pokedex.conditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.data.PokemobMatchInit;
import thut.api.util.JsonUtil;
import thut.lib.TComponent;

public interface PokemobCondition
{
    public static Map<String, Class<? extends PokemobCondition>> CONDITIONS = new HashMap<>();

    public static class ConditionAndWrapper implements PokemobCondition
    {
        final PokemobCondition wrapA;
        final PokemobCondition wrapB;

        public ConditionAndWrapper(PokemobCondition wrapA, PokemobCondition wrapB)
        {
            this.wrapA = wrapA;
            this.wrapB = wrapB;
        }

        @Override
        public boolean matches(IPokemob mobIn)
        {
            return wrapA.matches(mobIn) && wrapB.matches(mobIn);
        }
    }

    public static class ConditionOrWrapper implements PokemobCondition
    {
        final PokemobCondition wrapA;
        final PokemobCondition wrapB;

        public ConditionOrWrapper(PokemobCondition wrapA, PokemobCondition wrapB)
        {
            this.wrapA = wrapA;
            this.wrapB = wrapB;
        }

        @Override
        public boolean matches(IPokemob mobIn)
        {
            return wrapA.matches(mobIn) || wrapB.matches(mobIn);
        }
    }

    public static class ConditionNotWrapper implements PokemobCondition
    {
        final PokemobCondition wrap;

        public ConditionNotWrapper(PokemobCondition wrap)
        {
            this.wrap = wrap;
        }

        @Override
        public boolean matches(IPokemob mobIn)
        {
            return !wrap.matches(mobIn);
        }

        @Override
        public Component makeDescription()
        {
            Component base = wrap.makeDescription();
            if (base != null)
            {
                return TComponent.translatable("pokemob.description.negate", base);
            }
            return PokemobCondition.super.makeDescription();
        }
    }

    default PokemobCondition and(PokemobCondition other)
    {
        return new ConditionAndWrapper(this, other);
    }

    default PokemobCondition or(PokemobCondition other)
    {
        return new ConditionOrWrapper(this, other);
    }

    default PokemobCondition not()
    {
        return new ConditionNotWrapper(this);
    }

    boolean matches(IPokemob mobIn);

    default void init()
    {}

    default Component makeDescription()
    {
        return TComponent.literal("Missingno");
    }

    public static void addDescriptions(List<Component> comps, PokemobCondition condition)
    {
        if (condition instanceof ConditionAndWrapper AND)
        {
            addDescriptions(comps, AND.wrapA);
            addDescriptions(comps, AND.wrapB);
        }
        else
        {
            Component comp = condition.makeDescription();
            if (comp != null) comps.add(comp);
        }
    }

    public static List<Component> getDescriptions(PokemobCondition condition)
    {
        List<Component> comps = new ArrayList<>();
        addDescriptions(comps, condition);
        return comps;
    }

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
            PokecubeAPI.LOGGER.error("missing key {} for a pokemob rule!", obj);
            return null;
        }
        String key = obj.get("key").getAsString();
        Class<? extends PokemobCondition> condClass = CONDITIONS.get(key);
        if (condClass == null)
        {
            PokecubeAPI.LOGGER.error("invalid type key {} for a pokemob rule!", key);
            return null;
        }
        PokemobCondition condition =JsonUtil.gson.fromJson(obj, condClass);
        return  PokemobMatchInit.initMatchChecker(condition);
    }
}
