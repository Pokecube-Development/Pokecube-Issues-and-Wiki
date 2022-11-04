package pokecube.api.data.effects;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import pokecube.api.PokecubeAPI;
import pokecube.api.data.effects.actions.DamageAction;
import pokecube.api.data.effects.actions.DespawnAction;
import pokecube.api.data.effects.actions.IEffectAction;
import pokecube.api.data.effects.materials.Fluid;
import pokecube.api.data.effects.materials.IMaterialAction;
import pokecube.api.data.effects.materials.Light;
import thut.api.util.JsonUtil;

public class MaterialEffects
{
    public static Map<String, Class<? extends IEffectAction>> ACTIONS = Maps.newHashMap();
    public static Map<String, Class<? extends IMaterialAction>> MATERIALS = Maps.newHashMap();

    static
    {
        ACTIONS.put("damage", DamageAction.class);
        ACTIONS.put("despawn", DespawnAction.class);

        MATERIALS.put("light", Light.class);
        MATERIALS.put("fluid", Fluid.class);
    }

    public static List<IMaterialAction> fromJson(List<JsonObject> actions)
    {
        List<IMaterialAction> ret = Lists.newArrayList();
        actions.forEach(obj -> {
            String key = null;
            try
            {
                key = obj.has("type") ? obj.get("type").getAsString() : null;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (key == null)
            {
                PokecubeAPI.LOGGER.error("No key found for material {}", obj);
                return;
            }
            var clazz = MATERIALS.get(key);
            if (clazz == null)
            {
                PokecubeAPI.LOGGER.error("No material registered for key {}", key);
                return;
            }
            try
            {
                IMaterialAction action = JsonUtil.gson.fromJson(obj, clazz);
                action.init();
                ret.add(action);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
        return ret;
    }
}
