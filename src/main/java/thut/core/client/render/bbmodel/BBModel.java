package thut.core.client.render.bbmodel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import thut.api.entity.animation.Animation;
import thut.api.util.JsonUtil;
import thut.core.client.render.bbmodel.BBModelTemplate.JsonGroup;
import thut.core.client.render.model.BaseModel;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

public class BBModel extends BaseModel
{
    public BBModel()
    {
        super();
    }

    public BBModel(final ResourceLocation l)
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
            BBModelTemplate t = JsonUtil.gson.fromJson(reader, BBModelTemplate.class);
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

    private void makeObjects(BBModelTemplate t)
    {
        List<BBModelPart> parts = Lists.newArrayList();
        for (int i = 0; i < t.outliner.size(); i++)
        {
            JsonGroup b = t.outliner.get(i);
            float[] offset =  new float[] {0,0,0};
            BBModelPart.makeParts(t, b, parts, new ArrayList<>(),offset);
        }
        for (BBModelPart p : parts)
        {
            this.parts.put(p.getName(), p);
        }
    }

    @Override
    public void preProcessAnimations(Collection<Animation> collection)
    {

    }

}
