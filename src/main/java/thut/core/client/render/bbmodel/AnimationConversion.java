package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;
import org.nfunk.jep.JEP;

import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import thut.api.entity.IAnimated.MolangVars;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators.IAnimator;
import thut.api.entity.animation.Animators.KeyframeAnimator;
import thut.core.client.render.bbmodel.BBModelTemplate.BBAnimation.BBDataPoint;
import thut.core.client.render.bbmodel.BBModelTemplate.BBAnimation.BBKeyFrame;
import thut.core.common.ThutCore;

public class AnimationConversion
{
    private static final float deg_to_rads = (float) (Math.PI / 180f);

    public static String convertMolangToJEP(String molang)
    {
        String jep = molang;

        jep = jep.replaceAll("math.", "");// We do not need "math."

        // We take radians for these, not degreees.
        jep = jep.replace("sin(", "sin(%.4f * ".formatted(deg_to_rads));
        jep = jep.replace("cos(", "sin(%.4f * ".formatted(deg_to_rads));
        jep = jep.replace("tan(", "sin(%.4f * ".formatted(deg_to_rads));

        MolangVars.MOLANG_MAP.put("query.anim_time", "(t/20)");
        for (var entry : MolangVars.MOLANG_MAP.entrySet())
        {
            jep = jep.replace(entry.getKey(), entry.getValue());
        }

        if (jep.contains("query.") || jep.contains("q."))
        {
            ThutCore.LOGGER.error("Error with molang to jep conversion, was not complete! " + molang + " " + jep);
            jep = "0";
        }

        return jep;
    }

    public static class XMLAnimationSegment extends AnimationComponent
    {
        public String rotFuncs = "";
        public String scaleFuncs = "";
        public String posFuncs = "";

        public XMLAnimationSegment(int length, int start_time)
        {
            this.length = length;
            this.startKey = start_time;
        }
    }

    public static class BBModelAnimationSegment
    {
        final int time;
        boolean is_bedrock = false;
        boolean has_scale = false;
        Object[] rotations =
        { 0, 0, 0 };
        Object[] positions =
        { 0, 0, 0 };
        Object[] scales =
        { 0, 0, 0 };

        public BBModelAnimationSegment(double time)
        {
            this.time = (int) (time * 20);
        }

        public void process(BBKeyFrame keyframe)
        {
            String channel = keyframe.channel;
            List<BBDataPoint> points = keyframe.data_points;

            BBDataPoint data = points.get(0);

            Object x = data.x, y = data.y, z = data.z;
            try
            {
                if (data.x instanceof Double || data.x instanceof Integer)
                {
                    x = (double) data.x;
                }
                else if (data.x instanceof String)
                {
                    x = Double.parseDouble((String) data.x);
                }
            }
            catch (Exception e)
            {}
            try
            {
                if (data.y instanceof Double || data.y instanceof Integer)
                {
                    y = (double) data.y;
                }
                else if (data.y instanceof String)
                {
                    y = Double.parseDouble((String) data.y);
                }
            }
            catch (Exception e)
            {}
            try
            {
                if (data.z instanceof Double || data.z instanceof Integer)
                {
                    z = (double) data.z;
                }
                else if (data.z instanceof String)
                {
                    z = Double.parseDouble((String) data.z);
                }
            }
            catch (Exception e)
            {}

            switch (channel)
            {
            case "rotation":
                this.rotations[0] = x;
                this.rotations[1] = y;
                this.rotations[2] = z;
                break;
            case "position":
                this.positions[0] = x;
                this.positions[1] = y;
                this.positions[2] = z;
                break;
            case "scale":
                this.scales[0] = x;
                this.scales[1] = y;
                this.scales[2] = z;
                has_scale = true;
                break;
            }
        }

        private boolean setDoubles(double[] _to, Object[] _from, JEP[] jeps)
        {
            boolean allValid = true;
            for (int i = 0; i < 3; i++)
            {
                if (_from[i] instanceof Double)
                {
                    _to[i] = (double) _from[i];
                }
                else if (_from[i] instanceof String func)
                {
                    func = convertMolangToJEP(func);
                    jeps[i] = new JEP();
                    jeps[i].addStandardFunctions();
                    jeps[i].addStandardConstants();
                    for (var entry : MolangVars.JEP_VARS.entrySet())
                        if (func.contains(entry.getKey())) jeps[i].addVariable(entry.getKey(), entry.getValue());
                    jeps[i].parseExpression(func);
                    allValid = false;
                }
                else
                {
                    allValid = false;
                }
            }
            return allValid;
        }

        public boolean setDiff(double[] _arr, Object[] _pos, Object[] _neg, JEP[] jeps)
        {
            boolean allValid = true;
            for (int i = 0; i < 3; i++)
            {
                if (_pos[i] instanceof Double && _neg[i] instanceof Double)
                {
                    _arr[i] = (double) _pos[i] - (double) _neg[i];
                    if (Math.abs(_arr[i]) < 1e-4) _arr[i] = 0;
                }
                else if (_pos[i] instanceof String func)
                {
                    func = convertMolangToJEP(func);
                    jeps[i] = new JEP();
                    jeps[i].addStandardFunctions();
                    jeps[i].addStandardConstants();
                    for (var entry : MolangVars.JEP_VARS.entrySet())
                        if (func.contains(entry.getKey())) jeps[i].addVariable(entry.getKey(), entry.getValue());
                    jeps[i].parseExpression(func);
                    allValid = false;
                }
                else
                {
                    allValid = false;
                }
            }
            return allValid;
        }

        public XMLAnimationSegment toXML(BBModelAnimationSegment first_frame, BBModelAnimationSegment next_frame)
        {
            int start = this.time;
            int length = next_frame.time - this.time;

            if (first_frame == next_frame) start = 0;

            XMLAnimationSegment segment = new XMLAnimationSegment(length, start);

            boolean all_not_func = true;
            if (first_frame == this)
            {
                all_not_func = this.setDoubles(segment.posOffset, positions, segment._posFunctions) & all_not_func;
                all_not_func = this.setDoubles(segment.rotOffset, rotations, segment._rotFunctions) & all_not_func;
                all_not_func = this.setDoubles(segment.scaleOffset, scales, segment._scaleFunctions) & all_not_func;

                var old = segment.posOffset.clone();
                
                segment.posOffset[0] = -old[0] * 1 / 16f;
                segment.posOffset[1] = -old[2] * 1 / 16f;
                segment.posOffset[2] = +old[1] * 1 / 16f;

                if (is_bedrock) segment.posOffset[0] *= -1;

                segment.rotOffset[0] = -segment.rotOffset[0];
                segment.rotOffset[1] = -segment.rotOffset[1];

                if (has_scale)
                {
                    segment.scaleOffset[0] = segment.scaleOffset[0];
                    segment.scaleOffset[1] = segment.scaleOffset[1];
                    segment.scaleOffset[2] = segment.scaleOffset[2];
                    if (segment.scaleOffset[0] <= 0) segment.hidden = true;
                }
            }

            if (next_frame != first_frame)
            {
                all_not_func = this.setDiff(segment.posChange, this.positions, next_frame.positions,
                        segment._posFunctions) & all_not_func;
                all_not_func = this.setDiff(segment.rotChange, this.rotations, next_frame.rotations,
                        segment._rotFunctions) & all_not_func;
                all_not_func = this.setDiff(segment.scaleChange, this.scales, next_frame.scales,
                        segment._scaleFunctions) & all_not_func;

                segment.rotChange[0] = -segment.rotChange[0];
                segment.rotChange[1] = -segment.rotChange[1];
                
                var old = segment.posChange.clone();

                segment.posChange[0] = -old[0] * 1 / 16f;
                segment.posChange[1] = -old[2] * 1 / 16f;
                segment.posChange[2] = +old[1] * 1 / 16f;

                if (is_bedrock) segment.posChange[0] *= -1;

                if (has_scale)
                {
                    segment.scaleChange[0] = segment.scaleChange[0];
                    segment.scaleChange[1] = segment.scaleChange[1];
                    segment.scaleChange[2] = segment.scaleChange[2];
                    if (segment.scaleChange[0] <= 0) segment.hidden = true;
                }
            }

            if (!all_not_func)
            {}
            // We are not printing, so we don't need the "clear if not defined"
            // section

            return segment;
        }
    }

    public static Map<String, List<Animation>> make_animations(BBModelTemplate template)
    {
        Map<String, List<Animation>> map = new HashMap<>();
        boolean bedrock = template.meta.model_format.equals("bedrock");
        for (var animation : template.animations)
        {
            Map<String, List<AnimationComponent>> parts = new HashMap<>();
            for (var entry : animation.animators.entrySet())
            {
                var part = entry.getValue();
                Map<Double, BBModelAnimationSegment> segments = new Double2ObjectArrayMap<>();
                Map<String, List<BBModelAnimationSegment>> frames_map = new HashMap<>();
                // This puts them in correct order before this loop!
                part.keyframes.sort(null);
                for (var keyframe : part.keyframes)
                {
                    double time = keyframe.time;
                    var frame = segments.computeIfAbsent(time, t -> {
                        var f = new BBModelAnimationSegment(t);
                        List<BBModelAnimationSegment> frames_list = frames_map.getOrDefault(part.name, null);
                        if (frames_list == null) frames_map.put(part.name, frames_list = Lists.newArrayList());
                        frames_list.add(f);
                        f.is_bedrock = bedrock;
                        return f;
                    });
                    frame.process(keyframe);
                }
                for (var entry_frames : frames_map.entrySet())
                {
                    var frames = entry_frames.getValue();
                    List<AnimationComponent> xml_parts = new ArrayList<>();
                    var _part_name = entry_frames.getKey();
//                    System.out.println(_part_name);
                    if (parts.containsKey(_part_name))
                    {
                        xml_parts = parts.get(_part_name);
                    }
                    else
                    {
                        parts.put(_part_name, xml_parts);
                    }
                    if (frames.size() == 1)
                    {
                        var frame = frames.get(0);
                        xml_parts.add(frame.toXML(frame, frame));
                    }
                    else
                    {
                        var first_frame = frames.get(0);
                        for (int i = 0; i < frames.size() - 1; i++)
                        {
                            var next_frame = frames.get(i + 1);
                            var frame = frames.get(i);
                            var xml = frame.toXML(first_frame, next_frame);
                            xml_parts.add(xml);
                        }
                    }
                }
            }

            // Now here we differ from python a bit more, there it dumps as XML,
            // here we make into animation directly.

            var name = animation.name;
            Animation anmation = new Animation();
            anmation.name = name.replace(".", "");
            anmation.loops = animation.loop.equals("loop");

            List<Animation> anims = map.computeIfAbsent(animation.name, (k) -> {
                return new ArrayList<>();
            });
            anims.add(anmation);
            for (var entry : parts.entrySet())
            {
                var part = entry.getKey();
                List<AnimationComponent> list = entry.getValue();
                IAnimator animator = new KeyframeAnimator(list);
                if (anmation.sets.containsKey(part))
                {
                    ThutCore.LOGGER.warn("Unsupported double part for animation " + animation.name + " " + part);
                }
                else anmation.sets.put(part, animator);
            }
        }
        return map;
    }
}
