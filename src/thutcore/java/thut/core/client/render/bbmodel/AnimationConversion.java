package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import thut.api.entity.IAnimated.MolangVars;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators.KeyframeAnimator;
import thut.core.client.render.bbmodel.BBModelTemplate.BBAnimation.BBDataPoint;
import thut.core.client.render.bbmodel.BBModelTemplate.BBAnimation.BBKeyFrame;
import thut.core.client.render.model.parts.Part;
import thut.core.common.ThutCore;

public class AnimationConversion
{
    public static String convertMolangToJEP(String molang, boolean forcedLimbs)
    {
        String jep = molang;

        if (molang.contains("NaN"))
        {
            ThutCore.LOGGER.error("Warning, NaN in molang " + molang);
            jep = jep.replace("NaN", "0");
        }
        // Ignore case
        jep = jep.toLowerCase(Locale.ROOT);

        jep = jep.replaceAll("math.", "");// We do not need "math."

        // cleanup missing * symbols
        jep = jep.replace(")clamp", ")*clamp");
        jep = jep.replace(")sin", ")*sin");
        jep = jep.replace(")cos", ")*cos");
        jep = jep.replace(")tan", ")*tan");

        // We take radians for these, not degreees.
        jep = jep.replace("sin(", "sin_deg(");
        jep = jep.replace("cos(", "cos_deg(");
        jep = jep.replace("tan(", "tan_deg(");

        if (forcedLimbs)
        {
            jep = jep.replace("anim_time", "ground_speed");
        }

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
        static float[] _posFuncScales =
        { -1 / 16f, -1 / 16f, 1 / 16f };
        static float[] _rotFuncScales =
        { -1, -1, 1 };

        public String rotFuncs = "";
        public String scaleFuncs = "";
        public String posFuncs = "";

        public XMLAnimationSegment(float length, float start_time)
        {
            this.length = length;
            this.startKey = start_time;
            this._posFuncScale = _posFuncScales;
            this._rotFuncScale = _rotFuncScales;
        }
    }

    public static class BBModelAnimationSegment
    {
        final float time;
        boolean has_scale = false;
        boolean forcedLimbs = false;
        List<String> interpolations = new ArrayList<>();
        Object[] rotations =
        { null, null, null };
        Object[] positions =
        { null, null, null };
        Object[] scales =
        { null, null, null };
        String channel = "";

        public BBModelAnimationSegment(double time, boolean forcedLimbs)
        {
            this.time = (float) (time * 20);
            this.forcedLimbs = forcedLimbs;
            interpolations.add("linear");
            interpolations.add("linear");
            interpolations.add("linear");
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

            if (x instanceof String s && s.isBlank()) x = null;
            if (y instanceof String s && s.isBlank()) y = null;
            if (z instanceof String s && s.isBlank()) z = null;

            this.channel = channel;

            switch (channel)
            {
            case "rotation":
                this.rotations[0] = x;
                this.rotations[1] = y;
                this.rotations[2] = z;
                interpolations.set(0, keyframe.interpolation);
                break;
            case "position":
                this.positions[0] = x;
                this.positions[1] = y;
                this.positions[2] = z;
                interpolations.set(1, keyframe.interpolation);
                break;
            case "scale":
                this.scales[0] = x;
                this.scales[1] = z;
                this.scales[2] = y;
                interpolations.set(2, keyframe.interpolation);
                has_scale = true;
                break;
            }
        }

        private boolean setDoubles(double[] _to, Object[] _from, String[] jeps)
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
                    func = convertMolangToJEP(func, forcedLimbs);
                    jeps[i] = func;
                    allValid = false;
                }
            }
            return allValid;
        }

        public boolean setDiff(double[] _arr, Object[] _pos, Object[] _neg, String[] jeps)
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
                    func = convertMolangToJEP(func, forcedLimbs);
                    jeps[i] = func;
                    allValid = false;
                }
            }
            return allValid;
        }

        public XMLAnimationSegment toXML(BBModelAnimationSegment first_frame, BBModelAnimationSegment next_frame,
                double max_length)
        {
            float start = this.time;
            float length = next_frame.time - this.time;
            max_length *= 20;

            if (length <= 0) length = (float) max_length;
            length = (float) Math.min(max_length - start, length);

            if (first_frame == next_frame && !this.interpolations.contains("step")) start = 0;

            XMLAnimationSegment segment = new XMLAnimationSegment(length, start);

            segment.limbBased = this.forcedLimbs;

            boolean all_not_func = true;

            // Scoped for var old below
            {
                all_not_func = this.setDoubles(segment.posOffset, positions, segment._posFunctions) & all_not_func;
                all_not_func = this.setDoubles(segment.rotOffset, rotations, segment._rotFunctions) & all_not_func;
                all_not_func = this.setDoubles(segment.scaleOffset, scales, segment._scaleFunctions) & all_not_func;

                var old = segment.posOffset.clone();

                segment.posOffset[0] = -old[0] * 1 / 16f;
                segment.posOffset[1] = -old[2] * 1 / 16f;
                segment.posOffset[2] = +old[1] * 1 / 16f;

                old = segment.rotOffset.clone();

                segment.rotOffset[0] = -old[0];
                segment.rotOffset[1] = -old[1];
                segment.rotOffset[2] = +old[2];

                if (has_scale)
                {
                    segment.scaleOffset[0] = segment.scaleOffset[0];
                    segment.scaleOffset[1] = segment.scaleOffset[1];
                    segment.scaleOffset[2] = segment.scaleOffset[2];
                }
            }

            if (next_frame != first_frame)
            {

                if (!this.interpolations.get(0).contains("step"))
                {
                    all_not_func = this.setDiff(segment.rotChange, this.rotations, next_frame.rotations,
                            segment._rotFunctions) & all_not_func;
                    var old = segment.rotChange.clone();
                    segment.rotChange[0] = +old[0];
                    segment.rotChange[1] = +old[1];
                    segment.rotChange[2] = -old[2];
                }

                if (!this.interpolations.get(1).contains("step"))
                {
                    all_not_func = this.setDiff(segment.posChange, this.positions, next_frame.positions,
                            segment._posFunctions) & all_not_func;
                    var old = segment.posChange.clone();
                    segment.posChange[0] = +old[0] * 1 / 16f;
                    segment.posChange[1] = +old[2] * 1 / 16f;
                    segment.posChange[2] = -old[1] * 1 / 16f;
                }

                if (has_scale && !this.interpolations.get(2).contains("step"))
                {
                    all_not_func = this.setDiff(segment.scaleChange, this.scales, next_frame.scales,
                            segment._scaleFunctions) & all_not_func;
                    segment.scaleChange[0] = segment.scaleChange[0];
                    segment.scaleChange[1] = segment.scaleChange[1];
                    segment.scaleChange[2] = -segment.scaleChange[2];
                }
            }

            if (!all_not_func)
            {
                var old = segment._posFunctions.clone();
                segment._posFunctions[0] = old[0];
                segment._posFunctions[1] = old[2];
                segment._posFunctions[2] = old[1];
                segment._needJEPInit = true;

                if (length == 0 && max_length <= 0) segment.length = Integer.MAX_VALUE;
            }
            // We are not printing, so we don't need the "clear if not defined"
            // section
            segment._valid_channels.add(channel);

            return segment;
        }
    }

    public static Map<String, List<Animation>> make_animations(BBModelTemplate template, BBModel bbModel)
    {
        Map<String, List<Animation>> map = new HashMap<>();

        for (var animation : template.animations)
        {
            Map<String, List<AnimationComponent>> parts = new HashMap<>();
            for (var entry : animation.animators.entrySet())
            {
                var part = entry.getValue();

                Map<String, Map<String, List<BBModelAnimationSegment>>> frames_map = new HashMap<>();
                // This puts them in correct order before this loop!
                part.keyframes.sort(null);
                for (var keyframe : part.keyframes)
                {
                    boolean limb = bbModel.getParts().get(part.name) instanceof Part p && p.isOverridenLimb;
                    double time = keyframe.time;
                    var frame = new BBModelAnimationSegment(time, limb);
                    frame.process(keyframe);
                    Map<String, List<BBModelAnimationSegment>> part_frames_map = frames_map.get(part.name);
                    if (part_frames_map == null) frames_map.put(part.name, part_frames_map = new HashMap<>());

                    List<BBModelAnimationSegment> frames_list = part_frames_map.get(keyframe.channel);
                    if (frames_list == null) part_frames_map.put(keyframe.channel, frames_list = new ArrayList<>());
                    frames_list.add(frame);
                }

                for (var entry_frames : frames_map.entrySet())
                {
                    var part_frames_map = entry_frames.getValue();
                    List<AnimationComponent> xml_parts = new ArrayList<>();
                    var _part_name = entry_frames.getKey();

                    for (var entry_list : part_frames_map.entrySet())
                    {
                        var frames = entry_list.getValue();
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
                            var xml = frame.toXML(frame, frame, animation.length);
                            xml_parts.add(xml);
                        }
                        else
                        {
                            var first_frame = frames.get(0);
                            var last_frame = first_frame;
                            for (int i = 0; i < frames.size() - 1; i++)
                            {
                                var next_frame = frames.get(i + 1);
                                var frame = frames.get(i);
                                var xml = frame.toXML(first_frame, next_frame, animation.length);
                                xml_parts.add(xml);
                                last_frame = next_frame;
                            }
                            var xml = last_frame.toXML(first_frame, first_frame, animation.length);
                            if (xml._needJEPInit || last_frame.interpolations.contains("step")) xml_parts.add(xml);
                        }
                    }
                }
            }

            // Now here we differ from python a bit more, there it dumps as XML,
            // here we make into animation directly.

            var name = animation.name.replace("animation." + template.name + ".", "");
            name = ThutCore.trim(name);
            Animation anmation = new Animation();
            anmation.name = name;

            anmation.loops = animation.loop.equals("loop");
            List<Animation> anims = map.computeIfAbsent(animation.name, (k) -> {
                return new ArrayList<>();
            });
            anims.add(anmation);
            for (var entry : parts.entrySet())
            {
                var part = entry.getKey();
                List<AnimationComponent> list = entry.getValue();
                KeyframeAnimator animator = new KeyframeAnimator(list, true, (float) (animation.length * 20));
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
