package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;

import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators.KeyframeAnimator;
import thut.core.client.render.bbmodel.BBModelTemplate.BBAnimation.BBDataPoint;
import thut.core.client.render.bbmodel.BBModelTemplate.BBAnimation.BBKeyFrame;
import thut.core.common.ThutCore;

public class AnimationConversion
{
    private static class XMLAnimationSegment extends AnimationComponent
    {
        public XMLAnimationSegment(int length, int start_time)
        {
            this.length = length;
            this.startKey = start_time;
        }
    }

    private static class BBModelAnimationSegment
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

            Object x = 0, y = 0, z = 0;
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
                if (data.y instanceof Double || data.y instanceof Integer)
                {
                    y = (double) data.y;
                }
                else if (data.y instanceof String)
                {
                    y = Double.parseDouble((String) data.y);
                }
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

        private boolean setDoubles(double[] _to, Object[] _from)
        {
            boolean allValid = true;
            for (int i = 0; i < 3; i++)
            {
                if (_from[i] instanceof Double)
                {
                    _to[i] = (double) _from[i];
                }
                else
                {
                    allValid = false;
                }
            }
            return allValid;
        }

        public boolean setDiff(double[] _arr, Object[] _pos, Object[] _neg)
        {
            boolean allValid = true;
            for (int i = 0; i < 3; i++)
            {
                if (_pos[i] instanceof Double && _neg[i] instanceof Double)
                {
                    _arr[i] = (double) _pos[i] - (double) _neg[i];
                    if (Math.abs(_arr[i]) < 1e-4) _arr[i] = 0;
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
                all_not_func = this.setDoubles(segment.posOffset, positions) & all_not_func;
                all_not_func = this.setDoubles(segment.rotOffset, rotations) & all_not_func;
                all_not_func = this.setDoubles(segment.scaleOffset, scales) & all_not_func;

                segment.posOffset[0] = -segment.posOffset[0] * 1 / 16f;
                segment.posOffset[1] = -segment.posOffset[1] * 2 / 16f;
                segment.posOffset[2] = +segment.posOffset[2] * 0.5f / 16f;

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
                all_not_func = this.setDiff(segment.posChange, this.positions, next_frame.positions) & all_not_func;
                all_not_func = this.setDiff(segment.rotChange, this.rotations, next_frame.rotations) & all_not_func;
                all_not_func = this.setDiff(segment.scaleChange, this.scales, next_frame.scales) & all_not_func;
//                System.out.println("   s:" + segment.startKey + " l:" + segment.length + " r:"
//                        + Arrays.toString(segment.rotOffset) + " r:" + Arrays.toString(segment.rotChange) + " r:"
//                        + Arrays.toString(this.rotations) + " r:" + Arrays.toString(next_frame.rotations));
                segment.rotChange[0] = -segment.rotChange[0];
                segment.rotChange[1] = -segment.rotChange[1];
                
                segment.posChange[0] = -segment.posChange[0] * 1 / 16f;
                segment.posChange[1] = -segment.posChange[1] * 2 / 16f;
                segment.posChange[2] = +segment.posChange[2] * 0.5f / 16f;

                if (is_bedrock) segment.posChange[0] *= -1;

                if (has_scale)
                {
                    segment.scaleChange[0] = segment.scaleChange[0];
                    segment.scaleChange[1] = segment.scaleChange[1];
                    segment.scaleChange[2] = segment.scaleChange[2];
                    if (segment.scaleChange[0] <= 0) segment.hidden = true;
                }
            }

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
                var animator = new KeyframeAnimator(list);
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
