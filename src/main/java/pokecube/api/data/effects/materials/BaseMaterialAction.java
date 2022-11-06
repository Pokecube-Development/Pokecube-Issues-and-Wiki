package pokecube.api.data.effects.materials;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.effects.MaterialEffects;
import pokecube.api.data.effects.actions.IEffectAction;
import thut.api.util.JsonUtil;

public abstract class BaseMaterialAction implements IMaterialAction
{
    boolean replace = false;
    public List<JsonObject> actions = Lists.newArrayList();

    List<IEffectAction> _actions = Lists.newArrayList();

    @Override
    public void mergeFrom(IMaterialAction other)
    {
        if (other instanceof BaseMaterialAction action)
        {
            if (action.replace) actions.clear();
            this.actions.addAll(action.actions);
        }
    }

    @Override
    public void init()
    {
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
                PokecubeAPI.LOGGER.error("No key found for action {}", obj);
                return;
            }
            var clazz = MaterialEffects.ACTIONS.get(key);
            if (clazz == null)
            {
                PokecubeAPI.LOGGER.error("No action registered for key {}", key);
                return;
            }
            try
            {
                IEffectAction action = JsonUtil.gson.fromJson(obj, clazz);
                action.init();
                this._actions.add(action);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void applyEffect(LivingEntity mob)
    {
        if (this.shouldApply(mob)) this._actions.forEach(a -> a.applyEffect(mob));
    }
}
