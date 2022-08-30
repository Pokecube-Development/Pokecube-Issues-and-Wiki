package thut.core.client.render.json;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import thut.api.entity.animation.Animation;
import thut.api.util.JsonUtil;
import thut.core.client.render.json.JsonTemplate.JsonBlock;
import thut.core.client.render.model.BaseModel;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

public class JsonModel extends BaseModel
{
    public JsonModel()
    {
        super();
    }

    public JsonModel(final ResourceLocation l)
    {
        super(l);
    }

    @Override
    protected void loadModel(ResourceLocation model)
    {
        try
        {
            this.last_loaded = model;
            BufferedReader reader = ResourceHelper.getReader(model, Minecraft.getInstance().getResourceManager());
            if (reader == null)
            {
                this.valid = false;
                return;
            }
            JsonTemplate t = JsonUtil.gson.fromJson(reader, JsonTemplate.class);
            reader.close();
            t.init();
            this.makeObjects(t);
        }
        catch (Exception e)
        {
            this.valid = false;
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading " + model, e);
        }
    }

    private void makeObjects(JsonTemplate t)
    {
        List<JsonPart> parts = Lists.newArrayList();
        Map<Integer, List<JsonPart>> byIndex = Maps.newHashMap();
        for (int i = 0; i < t.elements.size(); i++)
        {
            JsonBlock b = t.elements.get(i);
            parts.addAll(JsonPart.makeParts(t, b, i));
        }
        for (JsonPart p : parts)
        {
            this.parts.put(p.getName(), p);
            List<JsonPart> bit = Lists.newArrayList();
            if (byIndex.containsKey(p.index)) bit = byIndex.get(p.index);
            else byIndex.put(p.index, bit);
            bit.add(p);
        }
    }

    @Override
    public void preProcessAnimations(Collection<Animation> collection)
    {

    }

}
