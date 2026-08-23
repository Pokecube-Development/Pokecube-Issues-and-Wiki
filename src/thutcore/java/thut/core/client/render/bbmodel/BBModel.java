package thut.core.client.render.bbmodel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators;
import thut.api.util.JsonUtil;
import thut.core.client.render.animation.AnimationXML;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.bbmodel.BBModelTemplate.JsonGroup;
import thut.core.client.render.model.BaseModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.parts.Part;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BBModel extends BaseModel
{
    private BBModelTemplate template;
    private final Set<String> builtin_anims = Sets.newHashSet();

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
            BufferedReader reader = ResourceHelper.getReader(model);
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
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading {}", model, e);
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

    @Override
    public void postInit()
    {
        super.postInit();
        // Cleanup the template, to save on memory
        this.template = null;
    }

    private void makeObjects(BBModelTemplate t)
    {
        List<BBModelPart> parts = Lists.newArrayList();

        if (t.outliner.isEmpty())
        {
            // We will make a single group, and just add everything to that.
            JsonGroup main = new JsonGroup();
            main.name = "root";
            main.origin = new float[] { 0, 0, 0 };
            main.children.addAll(t.elements);
        }
        t._materials.clear();
        for (int i = 0; i < t.outliner.size(); i++)
        {
            JsonGroup b = t.outliner.get(i);
            float[] parentOffsets = new float[] { 0, 0, 0 };
            BBModelPart.makeParts(t, b, parts, new ArrayList<>(), new HashSet<>(), parentOffsets);
        }
        for (BBModelPart p : parts)
        {
            var old_name = p.getName();
            int n = 1;
            while (this.parts.containsKey(p.getName()) && p instanceof Part p2)
            {
                // BB models don't map by name, and groups can also share names as parts
                // so here we just add to the end till it is new
                p2.name = old_name + "_" + n++;
            }
            this.parts.put(p.getName(), p);
            // Ensure the part is set to initial state
            p.resetToInit();
        }
    }

    @Override
    public void preProcessXMLAnimation(Animation animation)
    {
        Vector3f out = new Vector3f();
        Quaternionf quat = new Quaternionf();
        for (var a : animation.sets.values())
        {
            if (a instanceof Animators.KeyframeAnimator a2)
            {
                // First undo the results of "precompute"
                for (int i = a2.components.size()-1; i > 0; i--)
                {
                    AnimationComponent here = a2.components.get(i);
                    AnimationComponent prev = a2.components.get(i-1);
                    for (int j = 0; j < 3; j++)
                    {
                        here.rotOffset[j] -= prev.rotOffset[j] + prev.rotChange[j];
                    }
                }

                for(int i = 0; i<a2.components.size(); i++)
                {
                    var comp = a2.components.get(i);

                    double d0, d1, d2;
                    // These get adjusted so the coordinate system is
                    // consistant with the x3d cases
                    d0 = comp.posOffset[0] / 16;
                    d1 = comp.posOffset[1] / 16;
                    d2 = comp.posOffset[2] / 16;
                    //
                    comp.posOffset[0] = -d0;
                    comp.posOffset[1] = d2;
                    comp.posOffset[2] = -d1;
                    //
                    d0 = comp.posChange[0] / 16;
                    d1 = comp.posChange[1] / 16;
                    d2 = comp.posChange[2] / 16;
                    //
                    comp.posChange[0] = -d0;
                    comp.posChange[1] = d2;
                    comp.posChange[2] = -d1;

                    // Convert the rotations to new coordinate system
                    d0 = comp.rotOffset[0];
                    d1 = comp.rotOffset[1];
                    d2 = comp.rotOffset[2];
                    out.set(d0, d1, d2);
                    out.z *= -1;
                    out.x *= -1;
                    // Convert to radians for the quat maths
                    out.div((float) (180 / Math.PI));
                    quat.set(0, 0, 0, 1);
                    // XML rotations are funny when using all three axes...
                    if (d0 != 0 && d1 != 0 && d2 != 0)
                    {
                        quat.rotateX(out.x).rotateZ(out.z).rotateY(out.y);
                        quat.getEulerAnglesZXY(out);
                    }
                    else if (
                            (d0 != 0 && d1 == 0 && d2 == 0) ||
                            (d0 == 0 && d1 != 0 && d2 == 0) ||
                            (d0 == 0 && d1 == 0 && d2 != 0))
                    {
                        // Single rotation case, seems to be fine
                    }
                    else if (d0 != 0 && d1 != 0 && d2 == 0)
                    {
                        // This case is still broken for cases with large y angles
                        quat.rotateX(out.x);
                        quat.rotateY(out.y);
                        quat.getEulerAnglesZYX(out);
                    }
                    else if (d0 == 0 && d1 != 0 && d2 != 0)
                    {
                        // this case seems to work
                        quat.rotateZ(out.z);
                        quat.rotateY(out.y);
                        quat.getEulerAnglesZYX(out);
                    }
                    else
                    {
                        if (d0 != 0) quat.rotateX(out.x);
                        if (d2 != 0) quat.rotateZ(out.z);
                        if (d1 != 0) quat.rotateY(out.y);
                        quat.getEulerAnglesZYX(out);
                    }
                    // Convert back to degrees
                    out.mul((float) (180 / Math.PI));
                    comp.rotOffset[0] = out.x;
                    comp.rotOffset[1] = out.y;
                    comp.rotOffset[2] = out.z;

                    d0 = comp.rotChange[0];
                    d1 = comp.rotChange[1];
                    d2 = comp.rotChange[2];
                    out.set(d0, d1, d2);
                    out.z *= -1;
                    out.x *= -1;
                    // Convert to radians for the quat maths
                    out.div((float) (180 / Math.PI));
                    quat.set(0, 0, 0, 1);
                    // XML rotations are funny when using all three axes...
                    if (d0 != 0 && d1 != 0 && d2 != 0)
                    {
                        quat.rotateX(out.x).rotateZ(out.z).rotateY(out.y);
                        quat.getEulerAnglesZXY(out);
                    }
                    else if (
                            (d0 != 0 && d1 == 0 && d2 == 0) ||
                            (d0 == 0 && d1 != 0 && d2 == 0) ||
                            (d0 == 0 && d1 == 0 && d2 != 0))
                    {
                        // Single rotation case, seems to be fine
                    }
                    else if (d0 != 0 && d1 != 0 && d2 == 0)
                    {
                        // This case is still broken for cases with large y angles
                        quat.rotateX(out.x);
                        quat.rotateY(out.y);
                        quat.getEulerAnglesZYX(out);
                    }
                    else if (d0 == 0 && d1 != 0 && d2 != 0)
                    {
                        // this case seems to work
                        quat.rotateZ(out.z);
                        quat.rotateY(out.y);
                        quat.getEulerAnglesZYX(out);
                    }
                    else
                    {
                        if (d0 != 0) quat.rotateX(out.x);
                        if (d2 != 0) quat.rotateZ(out.z);
                        if (d1 != 0) quat.rotateY(out.y);
                        quat.getEulerAnglesZYX(out);
                    }
                    // Convert back to degrees
                    out.mul((float) (180 / Math.PI));
                    comp.rotChange[0] = out.x;
                    comp.rotChange[1] = out.y;
                    comp.rotChange[2] = out.z;
                }

                AnimationComponent prev = a2.components.getFirst();
                // now re-do the "precompute"
                for (int i = 1; i < a2.components.size(); i++)
                {
                    AnimationComponent here = a2.components.get(i);
                    for (int j = 0; j < 3; j++)
                    {
                        here.rotOffset[j] += prev.rotOffset[j] + prev.rotChange[j];
                    }
                    prev = here;
                }
            }
        }
    }

    @Override
    public void initModelMetadata(AnimationXML.ModelMetadata metaData)
    {
        metaData.headAxis = 2;
        metaData.headDir = -1;
        metaData.headAxis2 = 0;
        metaData.headDir2 = 1;
    }

    @Override
    public void updateMaterial(Mat mat)
    {
        if (mat.height < 0) mat.height = this.template.resolution.height;
        if (mat.width < 0) mat.width = this.template.resolution.width;
        super.updateMaterial(mat);
    }

}
