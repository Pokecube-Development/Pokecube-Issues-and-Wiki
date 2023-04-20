package thut.core.client.render.bbmodel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators.IAnimator;
import thut.api.entity.animation.Animators.KeyframeAnimator;
import thut.api.util.JsonUtil;
import thut.core.client.render.bbmodel.BBModelTemplate.BBAnimation.BBKeyFrame;
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
    public void initBuiltInAnimations(IModelRenderer<?> renderer)
    {
        for (var anim : template.animations)
        {
            // Each of these is a pre-baked animation for the part.
            Animation animation = new Animation();
            animation.name = anim.name.replace(".", "");
            animation.loops = true;
            builtin_anims.add(animation.name);
            System.out.println(animation.name);

            Map<String, List<AnimationComponent>> components = new HashMap<>();

            for (var entry : anim.animators.entrySet())
            {
                var value = entry.getValue();
                var part_name = value.name;
                boolean is_bone = "bone".equals(value.type);
                if (is_bone)
                {
                    List<AnimationComponent> comps = components.get(part_name);
                    if (comps == null) components.put(part_name, comps = new ArrayList<>());
                    Map<String, List<BBKeyFrame>> frame_types = new HashMap<>();

                    value.keyframes.sort(null);

                    for (var keyframe : value.keyframes)
                    {
                        List<BBKeyFrame> frames = frame_types.get(keyframe.channel);
                        if (frames == null) frame_types.put(keyframe.channel, frames = new ArrayList<>());
                        frames.add(keyframe);
                        for (var p : keyframe.data_points)
                        {
                            double x = 0, y = 0, z = 0;
                            try
                            {
                                if (p.x instanceof Double || p.x instanceof Integer)
                                {
                                    x = (double) p.x;
                                }
                                else if (p.x instanceof String)
                                {
                                    x = Double.parseDouble((String) p.x);
                                }
                                if (p.y instanceof Double || p.y instanceof Integer)
                                {
                                    y = (double) p.y;
                                }
                                else if (p.y instanceof String)
                                {
                                    y = Double.parseDouble((String) p.y);
                                }
                                if (p.z instanceof Double || p.z instanceof Integer)
                                {
                                    z = (double) p.z;
                                }
                                else if (p.z instanceof String)
                                {
                                    z = Double.parseDouble((String) p.z);
                                }
                            }
                            catch (Exception e)
                            {}
                            p.x = x;
                            p.y = y;
                            p.z = z;
                        }
                    }

                    List<BBKeyFrame> rotations = frame_types.get("rotation");
                    if (rotations != null)
                    {
                        List<AnimationComponent> rotComps = new ArrayList<>();
//                        System.out.println(part_name);

                        // Pre-compute the offsets
                        for (int i = 0; i < rotations.size(); i++)
                        {
                            var frame = rotations.get(i);
                            int length = (int) (frame.time * 20);
                            if (rotations.size() == 1) length = 0;
                            AnimationComponent comp = new AnimationComponent();
                            comp.startKey = length;
                            // First point
                            rotComps.add(comp);
                            for (var p : frame.data_points)
                            {
                                try
                                {
                                    double x = (double) p.x, y = (double) p.y, z = (double) p.z;
                                    comp.rotOffset[0] = -x;
                                    comp.rotOffset[1] = -y;
                                    comp.rotOffset[2] = z;
                                }
                                catch (Exception e)
                                {

                                }
                            }
                        }
                        
                        // Now compute differences for rot changes
                        for (int i = 1; i < rotations.size() + 1; i++)
                        {
                            var next_comp = i < rotations.size() ? rotComps.get(i)
                                    : i - 2 < 0 ? rotComps.get(0) : rotComps.get(i - 2);
                            var here_comp = rotComps.get(i - 1);
                            here_comp.rotChange[0] = next_comp.rotOffset[0] - here_comp.rotOffset[0];
                            here_comp.rotChange[1] = next_comp.rotOffset[1] - here_comp.rotOffset[1];
                            here_comp.rotChange[2] = next_comp.rotOffset[2] - here_comp.rotOffset[2];

                            here_comp.length = next_comp.startKey - here_comp.startKey;

                            if (here_comp.rotChange[0] != 0 || here_comp.rotChange[1] != 0
                                    || here_comp.rotChange[2] != 0)
                            {
                                if (here_comp.length < 0) here_comp.length = -here_comp.length;

                            }
//                            System.out.println(Arrays.toString(here_comp.rotChange) + " " + here_comp.length);
                        }
                        
                        // And then clear the offset for the not-first-components
                        for (int i = 1; i < rotations.size(); i++)
                        {
                            var comp = rotComps.get(i);
                            comp.rotOffset[0] = 0;
                            comp.rotOffset[1] = 0;
                            comp.rotOffset[2] = 0;
                        }

                        comps.addAll(rotComps);
                        Animation anmation = new Animation();
                        anmation.name = anim.name.replace(".", "");
                        anmation.loops = true;
                        builtin_anims.add(name);

                        var anims = renderer.getAnimations().get(anmation.name);
                        if (anims == null) renderer.getAnimations().put(anmation.name, anims = new ArrayList<>());
                        anims.add(anmation);
                        IAnimator animator = new KeyframeAnimator(rotComps);
                        anmation.sets.put(part_name, animator);

//                        System.out.println(animator.getLength() + " " + rotations.size());
                    }
                }
            }
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
        this.template = t;
    }

    @Override
    public void preProcessAnimations(Collection<Animation> collection)
    {}

}
