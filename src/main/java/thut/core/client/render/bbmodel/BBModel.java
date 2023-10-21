package thut.core.client.render.bbmodel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import thut.api.entity.animation.Animation;
import thut.api.util.JsonUtil;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.bbmodel.BBModelTemplate.JsonGroup;
import thut.core.client.render.model.BaseModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

public class BBModel extends BaseModel
{
    private BBModelTemplate template;
    private Set<String> builtin_anims = Sets.newHashSet();

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
            this.template = t;
            t.init();
            this.makeObjects(t);
        }
        catch (Exception e)
        {
            this.valid = false;
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading " + model, e);
        }
    }

    @Override
    public Set<String> getBuiltInAnimations()
    {
        return builtin_anims;
    }

    @Override
    public void initBuiltInAnimations(IModelRenderer<?> renderer, List<Animation> tblAnims)
    {
        var loaded = AnimationConversion.make_animations(this.template, this);
        this.builtin_anims.clear();
        for (var entry : loaded.entrySet())
        {
            String key = entry.getKey();
            var list = entry.getValue();
            this.builtin_anims.add(key);
            tblAnims.addAll(list);
        }
    }

    private void makeObjects(BBModelTemplate t)
    {
        List<BBModelPart> parts = Lists.newArrayList();

        if (t.outliner.isEmpty())
        {
            // We will make a single group, and just add everything to that.
            JsonGroup main = new JsonGroup();
            main.name = "root";
            main.origin = new float[]
            { 0, 0, 0 };
            main.children.addAll(t.elements);
        }
        t._materials.clear();
        for (int i = 0; i < t.outliner.size(); i++)
        {
            JsonGroup b = t.outliner.get(i);
            float[] parentOffsets = new float[]
            { 0, 0, 0 };
            BBModelPart.makeParts(t, b, parts, new ArrayList<>(), new HashSet<>(), parentOffsets);
        }
        for (BBModelPart p : parts)
        {
            this.parts.put(p.getName(), p);
        }
    }

    @Override
    public void updateMaterial(Mat mat)
    {
        if (mat.height < 0) mat.height = this.template.resolution.height;
        if (mat.width < 0) mat.width = this.template.resolution.width;
        super.updateMaterial(mat);
    }

    @Override
    public void preProcessAnimations(Collection<Animation> collection)
    {}

}
