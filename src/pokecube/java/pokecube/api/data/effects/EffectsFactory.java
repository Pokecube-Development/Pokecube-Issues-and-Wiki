package pokecube.api.data.effects;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import pokecube.api.PokecubeAPI;
import pokecube.api.data.effects.actions.DamageAction;
import pokecube.api.data.effects.actions.DespawnAction;
import pokecube.api.data.effects.actions.HappinessAction;
import pokecube.api.data.effects.actions.HungerAction;
import pokecube.api.data.effects.actions.IEffectAction;
import pokecube.api.data.effects.materials.Fluid;
import pokecube.api.data.effects.materials.IMaterialAction;
import pokecube.api.data.effects.materials.Light;
import thut.api.util.JsonUtil;

public class EffectsFactory
{
    public static Map<String, Class<? extends IEffectAction>> ACTIONS = Maps.newHashMap();
    public static Map<String, Class<? extends IMaterialAction>> MATERIALS = Maps.newHashMap();

    static
    {
        ACTIONS.put("hunger", HungerAction.class);
        ACTIONS.put("happiness", HappinessAction.class);
        ACTIONS.put("damage", DamageAction.class);
        ACTIONS.put("despawn", DespawnAction.class);

        MATERIALS.put("light", Light.class);
        MATERIALS.put("fluid", Fluid.class);
    }

    public static List<IEffectAction> fromJson(List<JsonObject> actions)
    {
        List<IEffectAction> ret = Lists.newArrayList();
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
                // instead check if it is a regular action, then add that instead
                var clazz2 = EffectsFactory.ACTIONS.get(key);
                if (clazz2 == null)
                {
                    PokecubeAPI.LOGGER.error("No material or action registered for key {}", key);
                    return;
                }
                try
                {
                    IEffectAction action = JsonUtil.gson.fromJson(obj, clazz2);
                    action.init();
                    ret.add(action);
                    return;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
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
