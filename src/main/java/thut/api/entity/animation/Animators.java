package thut.api.entity.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.IAnimated.MolangVars;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.parts.Part;
import thut.core.common.ThutCore;

public class Animators
{
    public static interface IAnimator
    {
        boolean animate(Animation animation, IAnimationHolder holder, IExtendedModelPart part, float partialTick,
                float limbSwing, int tick);

        default void setColours(int... rgba)
        {}

        int getLength();

        boolean hasLimbBased();

        void setLimbBased();

        void setHidden(boolean hidden);

        default boolean conflicts(IAnimator other)
        {
            return true;
        }
    }

    public static void fillJEPs(JEP[] jeps, String _funcs)
    {
        String[] funcs = _funcs.split(",");
        func:
        for (String s : funcs)
        {
            String[] args = s.split(":");
            int i;
            switch (args[0])
            {
            case ("x"):
                i = 0;
                break;
            case ("y"):
                i = 1;
                break;
            case ("z"):
                i = 2;
                break;
            case ("rx"):
                i = 0;
                break;
            case ("ry"):
                i = 1;
                break;
            case ("rz"):
                i = 2;
                break;
            case ("dx"):
                i = 0;
                break;
            case ("dy"):
                i = 1;
                break;
            case ("dz"):
                i = 2;
                break;
            default:
                ThutCore.LOGGER.error("Malformed function animation {}", s);
                continue func;
            }
            var func = args[1];
            jeps[i] = new JEP();
            jeps[i].addStandardFunctions();
            jeps[i].addStandardConstants();
            for (var entry : MolangVars.JEP_VARS.entrySet())
                if (func.contains(entry.getKey())) jeps[i].addVariable(entry.getKey(), entry.getValue());
            jeps[i].parseExpression(func);
        }
    }

    private static final Vector4 _rot = new Vector4();

    public static class KeyframeAnimator implements IAnimator
    {
        private static final AnimationComponent DEFAULTS = new AnimationComponent();

        private static record AnimChannel(String channel, List<AnimationComponent> components, int length)
        {
        };

        float[] dr = new float[3];
        float[] ds = new float[3];
        float[] dx = new float[3];

        public final List<AnimationComponent> components;
        public final List<AnimChannel> channels = new ArrayList<>();
        Map<String, AnimChannel> channel_map = new HashMap<>();
        private int length = -1;
        private boolean limbBased;
        private boolean hidden = false;

        public KeyframeAnimator(AnimationComponent component)
        {
            this(Lists.newArrayList(component), false);
        }

        public KeyframeAnimator(List<AnimationComponent> components)
        {
            this(components, false);
        }

        public KeyframeAnimator(List<AnimationComponent> components, boolean preComputed)
        {
            this.components = components;
            this.length = -1;
            this.limbBased = false;
            Map<String, List<AnimationComponent>> by_channel = new HashMap<>();
            for (final AnimationComponent component : components)
            {
                this.length = Math.max(this.length, component.startKey + component.length);
                this.limbBased = this.limbBased || component.limbBased;

                // Set anything close enough to 0 to 0. This fixes -0.0 != 0.0
                // in the below equals check
                for (int i = 0; i < 3; i++)
                {
                    if (Math.abs(component.posChange[i]) < 1e-5) component.posChange[i] = 0;
                    if (Math.abs(component.posOffset[i]) < 1e-5) component.posOffset[i] = 0;
                    if (Math.abs(component.rotChange[i]) < 1e-5) component.rotChange[i] = 0;
                    if (Math.abs(component.rotOffset[i]) < 1e-5) component.rotOffset[i] = 0;
                }

                boolean position_channel = !Arrays.equals(component.posChange, DEFAULTS.posChange);
                position_channel |= !Arrays.equals(component.posOffset, DEFAULTS.posOffset);
                position_channel |= !Arrays.equals(component._posFunctions, DEFAULTS._posFunctions);

                boolean rotation_channel = !Arrays.equals(component.rotChange, DEFAULTS.rotChange);
                rotation_channel |= !Arrays.equals(component.rotOffset, DEFAULTS.rotOffset);
                rotation_channel |= !Arrays.equals(component._rotFunctions, DEFAULTS._rotFunctions);

                boolean scale_channel = !Arrays.equals(component.scaleChange, DEFAULTS.scaleChange);
                scale_channel |= !Arrays.equals(component.scaleOffset, DEFAULTS.scaleOffset);
                scale_channel |= !Arrays.equals(component._scaleFunctions, DEFAULTS._scaleFunctions);

                boolean opac_channel = component._opacFunction != null;
                opac_channel |= component.opacityChange != DEFAULTS.opacityChange;
                opac_channel |= component.opacityOffset != DEFAULTS.opacityOffset;

                if (position_channel) component._valid_channels.add("position");
                if (rotation_channel) component._valid_channels.add("rotation");
                if (scale_channel) component._valid_channels.add("scale");
                if (opac_channel) component._valid_channels.add("opacity");

                component._foundNoJEP = Arrays.equals(component._posFunctions, DEFAULTS._posFunctions);
                component._foundNoJEP &= Arrays.equals(component._rotFunctions, DEFAULTS._rotFunctions);
                component._foundNoJEP &= Arrays.equals(component._scaleFunctions, DEFAULTS._scaleFunctions);

                for (String channel : component._valid_channels)
                {
                    var list = by_channel.computeIfAbsent(channel, c -> new ArrayList<>());
                    list.add(component);
                }
            }

            for (var entry : by_channel.entrySet())
            {
                String key = entry.getKey();
                var list = entry.getValue();
                int len = 1;
                for (var comp : list) len = Math.max(len, comp.startKey + comp.length);
                AnimChannel channel = new AnimChannel(key, list, len);
                this.channels.add(channel);
                this.channel_map.put(key, channel);
            }

            if (!preComputed) for (var entry : by_channel.entrySet())
            {
                var list = entry.getValue();
                AnimationComponent prev = list.get(0);
                for (int i = 1; i < list.size(); i++)
                {
                    AnimationComponent here = list.get(i);
                    for (int j = 0; j < 3; j++)
                    {
                        here.posOffset[j] += prev.posOffset[j] + prev.posChange[j];
                        here.rotOffset[j] += prev.rotOffset[j] + prev.rotChange[j];
                    }
                    prev = here;
                }
            }

        }

        private AnimationComponent getNext(float time1, float time2, boolean loops, String channel)
        {
            var animChannel = channel_map.getOrDefault(channel, null);
            if (animChannel == null) return null;
            var components = animChannel.components();
            if (components.isEmpty()) return null;
            int n = components.size();

            if (loops)
            {
                time1 %= animChannel.length();
                time2 %= animChannel.length();
            }

            // Now we run through the components per channel
            AnimationComponent component = components.get(n - 1);
            for (int i = 0; i < n - 1; i++)
            {
                var tmp = components.get(i + 1);
                final float time = component.limbBased ? time2 : time1;
                if (tmp.startKey > time)
                {
                    component = components.get(i);
                    break;
                }
            }
            return component;
        }

        public boolean animateJEP(Animation animation, AnimationComponent component, IExtendedModelPart part,
                MolangVars molangs, String channel)
        {
            if (component._foundNoJEP) return false;
            if (hidden)
            {
                part.setHidden(true);
                return true;
            }
            int modifies = 0;
            for (int i = 0; i < 3; i++)
            {
                switch (channel)
                {
                case "rotation":
                    if (component._rotFunctions[i] != null)
                    {
                        molangs.updateJEP(component._rotFunctions[i]);
                        dr[i] = (float) component._rotFunctions[i].getValue() * component._rotFuncScale[i];
                        modifies++;
                    }
                    break;
                case "position":
                    if (component._posFunctions[i] != null)
                    {
                        molangs.updateJEP(component._posFunctions[i]);
                        dx[i] = (float) component._posFunctions[i].getValue() * component._posFuncScale[i];
                        modifies++;
                    }
                    break;
                case "scale":
                    if (component._scaleFunctions[i] != null)
                    {
                        molangs.updateJEP(component._scaleFunctions[i]);
                        ds[i] = (float) component._scaleFunctions[i].getValue() * component._scaleFuncScale[i];
                        modifies++;
                    }
                    break;
                }
            }
            return modifies > 0;
        }

        @Override
        public boolean animate(Animation animation, IAnimationHolder holder, IExtendedModelPart part, float partialTick,
                float limbSwing, int tick)
        {
            boolean animated = false;
            final Vector3 temp = animation._shift.clear();

            float px = 0, py = 0, pz = 0;
            float rx = 0, ry = 0, rz = 0;
            float sx = 1, sy = 1, sz = 1;
            float alpha_scale = 1.0f;
            float time1;
            float time2;
            MolangVars molangs = holder.getMolangVars();

            time1 = (float) molangs.getAnimTime();
            time2 = (float) molangs.l;

            int aniTick = (int) Math.ceil(time1);

//            molangs.t = time1 = 1.f * 20f;

            // First clear these
            dr[0] = dr[1] = dr[2] = 0;
            dx[0] = dx[1] = dx[2] = 0;
            ds[0] = ds[1] = ds[2] = 1;

            // Marker for if any were hidden
            boolean any_hidden = false;

            boolean limb = part instanceof Part p && p.isOverridenLimb;

            Set<String> used = new HashSet<>();
            // Now we run through the components per channel

            String channel = "rotation";
            // Rotation set
            rots:
            {
                AnimationComponent component = getNext(time1, time2, animation.loops, channel);
                if (component == null) break rots;
                animated = true;
                float time = component.limbBased || limb ? time2 : time1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));
                if (animation.loops)
                {
                    var animChannel = channel_map.getOrDefault(channel, null);
                    time %= animChannel.length();
                }

                any_hidden |= component.hidden;
                used.add(channel);

                // Start by checking JEP components to the animation
                animateJEP(animation, component, part, molangs, channel);
                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final int length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;

                rx += component.rotChange[0] * ratio + component.rotOffset[0];
                ry += component.rotChange[1] * ratio + component.rotOffset[1];
                rz += component.rotChange[2] * ratio + component.rotOffset[2];
            }

            channel = "position";
            // Position set
            pos:
            {
                AnimationComponent component = getNext(time1, time2, animation.loops, channel);
                if (component == null) break pos;
                animated = true;
                float time = component.limbBased || limb ? time2 : time1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));
                if (animation.loops)
                {
                    var animChannel = channel_map.getOrDefault(channel, null);
                    time %= animChannel.length();
                }

                any_hidden |= component.hidden;
                used.add(channel);

                // Start by checking JEP components to the animation
                animateJEP(animation, component, part, molangs, channel);
                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final int length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;

                px += component.posChange[0] * ratio + component.posOffset[0];
                py += component.posChange[1] * ratio + component.posOffset[1];
                pz += component.posChange[2] * ratio + component.posOffset[2];
            }

            channel = "scale";
            // scale set
            scales:
            {
                AnimationComponent component = getNext(time1, time2, animation.loops, channel);
                if (component == null) break scales;
                animated = true;
                float time = component.limbBased || limb ? time2 : time1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));
                if (animation.loops)
                {
                    var animChannel = channel_map.getOrDefault(channel, null);
                    time %= animChannel.length();
                }

                any_hidden |= component.hidden;
                used.add(channel);

                // Start by checking JEP components to the animation
                animateJEP(animation, component, part, molangs, channel);
                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final int length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;

                sx *= component.scaleChange[0] * ratio + component.scaleOffset[0];
                sy *= component.scaleChange[1] * ratio + component.scaleOffset[1];
                sz *= component.scaleChange[2] * ratio + component.scaleOffset[2];
            }

            channel = "opacity";
            // scale set
            opacity:
            {
                AnimationComponent component = getNext(time1, time2, animation.loops, channel);
                if (component == null) break opacity;
                animated = true;
                any_hidden |= component.hidden;
                if (component._opacFunction != null)
                {
                    molangs.updateJEP(component._opacFunction);
                    alpha_scale *= component._opacFunction.getValue();
                }
                float time = component.limbBased || limb ? time2 : time1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));
                if (animation.loops)
                {
                    var animChannel = channel_map.getOrDefault(channel, null);
                    time %= animChannel.length();
                }
                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final int length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;
                
                alpha_scale *= component.opacityOffset + ratio * component.opacityChange;
            }
            
            if(any_hidden) System.out.println(part.getName() + " " + time1);

            // Apply hidden like this so last hidden state is kept
            part.setHidden(any_hidden);
            part.setOpacityScale(alpha_scale);
            holder.setStep(animation, aniTick);
            if (animated)
            {
                temp.set(px, py, pz);

//                if (part.getName().equals("torso"))// leg_front_right
//                {
//                    System.out.println(part.getName() + " " + time1);
//                    System.out.println(used);
//                    System.out.println(temp + " " + Arrays.toString(dx) + " " + rx + " " + ry + " " + rz);
//                }

                rx += dr[0];
                ry += dr[1];
                rz += dr[2];

                temp.addTo(dx[0], dx[1], dx[2]);

                sx *= ds[0];
                sy *= ds[1];
                sz *= ds[2];
                part.setPreTranslations(temp);
                part.setPreScale(temp.set(sx, sy, sz));
                final Quaternion quat = new Quaternion(0, 0, 0, 1);
                if (rz != 0) quat.mul(Vector3f.YN.rotationDegrees(rz));
                if (rx != 0) quat.mul(Vector3f.XP.rotationDegrees(rx));
                if (ry != 0) quat.mul(Vector3f.ZP.rotationDegrees(ry));
                part.setPreRotations(_rot.set(quat));
            }

            return animated;
        }

        @Override
        public int getLength()
        {
            return this.length;
        }

        @Override
        public boolean hasLimbBased()
        {
            return limbBased;
        }

        @Override
        public void setLimbBased()
        {
            limbBased = true;
        }

        @Override
        public void setHidden(boolean hidden)
        {
            this.hidden = hidden;
            for (final AnimationComponent component : components) component.hidden = hidden;
        }

        @Override
        public boolean conflicts(IAnimator other)
        {
            if (other instanceof KeyframeAnimator anim)
            {
                Set<String> our_channels = this.channel_map.keySet();
                Set<String> other_channels = anim.channel_map.keySet();
                return Sets.intersection(our_channels, other_channels).isEmpty();
            }
            return IAnimator.super.conflicts(other);
        }
    }
}
