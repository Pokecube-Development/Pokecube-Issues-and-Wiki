package thut.core.client.render.json;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import thut.api.entity.animation.Animation;
import thut.api.util.JsonUtil;
import thut.core.client.render.json.JsonTemplate.JsonBlock;
import thut.core.client.render.model.BaseModel;
import thut.core.common.ThutCore;

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
            final Resource res = Minecraft.getInstance().getResourceManager().getResource(model);
            if (res == null)
            {
                this.valid = false;
                return;
            }
            JsonTemplate t = JsonUtil.gson.fromJson(new InputStreamReader(res.getInputStream()), JsonTemplate.class);
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
            parts.addAll(JsonPart.makeParts(b, i));
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
