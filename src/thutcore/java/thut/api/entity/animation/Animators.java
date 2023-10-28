package thut.api.entity.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.IAnimated.MolangVars;
import thut.api.maths.Vector3;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.parts.Part;
import thut.core.common.ThutCore;

public class Animators
{

    // This matches <number> * <number>, where <number> is a floating point
    // value
    // ([-]*[0-9]+[.]?[0-9]*) matches floating point number
    // There can also be spaces between
    private static final Pattern multiply_pattern = Pattern
            .compile("([-]*[0-9]+[.]?[0-9]*)\\s*\\*\\s*([-]*[0-9]+[.]?[0-9]*)");

    // this matches <number> + <number>
    // The (?<![*.\\-0-9])(?<![*/]\s*) prevents it matching * in front
    // ([-]*[0-9]+[.]?[0-9]*)\s*\+\s*([-]*[0-9]+[.]?[0-9]*) matches floating
    // point numbers with a + between
    // the (?!\*) pevents it matching * after
    private static final Pattern add_pattern = Pattern
            .compile("(?<![*.\\-0-9])(?<![*/]\\s*)([-]*[0-9]+[.]?[0-9]*)\\s*\\+\\s*([-]*[0-9]+[.]?[0-9]*)(?![*.0-9])");

    private static final Pattern subtract_pattern = Pattern
            .compile("(?<![*.\\-0-9])(?<![*/]\\s*)([-]*[0-9]+[.]?[0-9]*)\\s*\\-\\s*([-]*[0-9]+[.]?[0-9]*)(?![*.0-9])");

    private static final Function<MatchResult, String> multiply = (m) -> {
        String var = m.group(1);
        String var_2 = m.group(2);
        return "" + Float.parseFloat(var) * Float.parseFloat(var_2);
    };
    private static final Function<MatchResult, String> add = (m) -> {
        String var = m.group(1);
        String var_2 = m.group(2);
        return "" + (Float.parseFloat(var) + Float.parseFloat(var_2));
    };
    private static final Function<MatchResult, String> subtract = (m) -> {
        String var = m.group(1);
        String var_2 = m.group(2);
        return "" + (Float.parseFloat(var) - Float.parseFloat(var_2));
    };

    public static interface IAnimator
    {
        boolean animate(Animation animation, IAnimationHolder holder, IExtendedModelPart part, float partialTick,
                float limbSwing, int tick);

        default void setColours(int... rgba)
        {}

        float getLength();

        boolean hasLimbBased();

        void setLimbBased();

        void setHidden(boolean hidden);

        default boolean conflicts(IAnimator other)
        {
            return true;
        }
    }

    public static void fillJEPs(String[] jeps, String _funcs)
    {
        String[] funcs = _funcs.split("::");
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
            case ("r"):
                i = 0;
                break;
            case ("g"):
                i = 1;
                break;
            case ("b"):
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
            jeps[i] = func;
        }
    }

    public static class KeyframeAnimator implements IAnimator
    {
        private static final AnimationComponent DEFAULTS = new AnimationComponent();

        public static enum CHANNEL
        {
            POS("position"), ROT("rotation"), SCALE("scale"), OPACITY("opacity"), COLOUR("colour"), HIDDEN("hidden");

            private final String name;

            private CHANNEL(String name)
            {
                this.name = name;
            }

            public String getName()
            {
                return name;
            }
        }

        private static record AnimChannel(String channel, List<AnimationComponent> components, float length)
        {
        };

        float[] dr = new float[3];
        float[] ds = new float[3];
        float[] dc = new float[3];
        float[] dx = new float[3];

        public final List<AnimationComponent> components;
        public final List<AnimChannel> channels = new ArrayList<>();
        private final Set<CHANNEL> channelSet = new HashSet<>();
        private float length = -1;
        private boolean limbBased;
        private boolean hidden = false;

        public KeyframeAnimator(AnimationComponent component)
        {
            this(Lists.newArrayList(component), false, -1);
        }

        public KeyframeAnimator(List<AnimationComponent> components)
        {
            this(components, false, -1);
        }

        public KeyframeAnimator(List<AnimationComponent> components, boolean preComputed, float baseLength)
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

                boolean colour_channel = !Arrays.equals(component.colChange, DEFAULTS.colChange);
                colour_channel |= !Arrays.equals(component.colOffset, DEFAULTS.colOffset);
                colour_channel |= !Arrays.equals(component._colFunctions, DEFAULTS._colFunctions);

                boolean opac_channel = component._opacFunction != null;
                opac_channel |= component.opacityChange != DEFAULTS.opacityChange;
                opac_channel |= component.opacityOffset != DEFAULTS.opacityOffset;

                if (position_channel) component._valid_channels.add("position");
                if (rotation_channel) component._valid_channels.add("rotation");
                if (scale_channel) component._valid_channels.add("scale");
                if (opac_channel) component._valid_channels.add("opacity");
                if (colour_channel) component._valid_channels.add("colour");
                if (component.hidden) component._valid_channels.add("hidden");

                component._needJEPInit = component._opacFunction != DEFAULTS._opacFunction;
                component._needJEPInit |= !Arrays.equals(component._posFunctions, DEFAULTS._posFunctions);
                component._needJEPInit |= !Arrays.equals(component._rotFunctions, DEFAULTS._rotFunctions);
                component._needJEPInit |= !Arrays.equals(component._colFunctions, DEFAULTS._colFunctions);
                component._needJEPInit |= !Arrays.equals(component._scaleFunctions, DEFAULTS._scaleFunctions);

                for (String channel : component._valid_channels)
                {
                    var list = by_channel.computeIfAbsent(channel, c -> new ArrayList<>());
                    list.add(component);
                }
            }

            for (var entry : by_channel.entrySet())
            {
                var list = entry.getValue();
                AnimationComponent prev = list.get(0);
                boolean position = entry.getKey().equals("position");
                boolean rotation = entry.getKey().equals("rotation");

                for (int i = 1; i < list.size(); i++)
                {
                    AnimationComponent here = list.get(i);
                    if (!preComputed)
                    {
                        for (int j = 0; j < 3 && (position || rotation); j++)
                        {
                            if (position) here.posOffset[j] += prev.posOffset[j] + prev.posChange[j];
                            if (rotation) here.rotOffset[j] += prev.rotOffset[j] + prev.rotChange[j];
                        }
                        here.startKey = prev.startKey + prev.length;
                    }
                    prev = here;
                }
                if (!preComputed) prev.length = Math.max(baseLength - prev.startKey, prev.length);
            }

            for (var channelEnum : CHANNEL.values())
            {
                String key = channelEnum.getName();
                var list = by_channel.get(key);
                if (list != null)
                {
                    float len = 1;
                    for (var comp : list) len = Math.max(len, comp.startKey + comp.length);
                    AnimChannel channel = new AnimChannel(key, list, len);
                    this.channels.add(channel);
                    this.channelSet.add(channelEnum);
                }
                else this.channels.add(null);
            }

            // lastly compute length, as the loop above could have changed
            // things
            for (final AnimationComponent component : components)
            {
                this.length = Math.max(this.length, component.startKey + component.length);
                this.limbBased = this.limbBased || component.limbBased;
            }
        }

        private String cleanFunc(String func)
        {
            if (func == null) return func;
            var m = multiply_pattern.matcher(func);
            String new_func = func.replace(" ", "").replace("\n", "");
            // Multiplications first
            while (m.find())
            {
                new_func = m.replaceAll(multiply);
                m = multiply_pattern.matcher(new_func);
            }
            // Then additions
            m = add_pattern.matcher(new_func);
            while (m.find())
            {
                new_func = m.replaceAll(add);
                m = add_pattern.matcher(new_func);
            }
            // finally subtractions
            m = subtract_pattern.matcher(new_func);
            while (m.find())
            {
                new_func = m.replaceAll(subtract);
                m = subtract_pattern.matcher(new_func);
            }
            return new_func;
        }

        private AnimationComponent getNext(float time1, float time2, boolean loops, AnimChannel animChannel)
        {
            var components = animChannel.components();
            if (components.isEmpty()) return null;
            int n = components.size();

            // Now we run through the components per channel
            AnimationComponent component = components.get(n - 1);
            float time = component.limbBased ? time2 : time1;
            if (time < component.startKey) component = null; // Not started yet
            for (int i = 0; i < n - 1; i++)

            {
                var next = components.get(i + 1);
                var here = components.get(i);
                time = next.limbBased ? time2 : time1;
                // Here has started, and not next started yet
                if (time >= here.startKey && next.startKey > time)
                {
                    component = here;
                    break;
                }
            }
            if (component != null && component._needJEPInit)
            {
                component._needJEPInit = false;
                String func = cleanFunc(component._opacFunction);
                if (func != null)
                {
                    component._opacJEP = new JEP();
                    component._opacJEP.addStandardFunctions();
                    component._opacJEP.addStandardConstants();
                    for (var entry : MolangVars.JEP_VARS.entrySet()) if (func.contains(entry.getKey()))
                        component._opacJEP.addVariable(entry.getKey(), entry.getValue());
                    component._opacJEP.parseExpression(func);
                }
                for (int i = 0; i < 3; i++)
                {
                    func = cleanFunc(component._posFunctions[i]);
                    if (func != null)
                    {
                        component._posJEPs[i] = new JEP();
                        component._posJEPs[i].addStandardFunctions();
                        component._posJEPs[i].addStandardConstants();
                        for (var entry : MolangVars.JEP_VARS.entrySet()) if (func.contains(entry.getKey()))
                            component._posJEPs[i].addVariable(entry.getKey(), entry.getValue());
                        component._posJEPs[i].parseExpression(func);
                    }
                    func = cleanFunc(component._rotFunctions[i]);
                    if (func != null)
                    {
                        component._rotJEPs[i] = new JEP();
                        component._rotJEPs[i].addStandardFunctions();
                        component._rotJEPs[i].addStandardConstants();
                        for (var entry : MolangVars.JEP_VARS.entrySet()) if (func.contains(entry.getKey()))
                            component._rotJEPs[i].addVariable(entry.getKey(), entry.getValue());
                        component._rotJEPs[i].parseExpression(func);
                    }
                    func = cleanFunc(component._colFunctions[i]);
                    if (func != null)
                    {
                        component._colJEPs[i] = new JEP();
                        component._colJEPs[i].addStandardFunctions();
                        component._colJEPs[i].addStandardConstants();
                        for (var entry : MolangVars.JEP_VARS.entrySet()) if (func.contains(entry.getKey()))
                            component._colJEPs[i].addVariable(entry.getKey(), entry.getValue());
                        component._colJEPs[i].parseExpression(func);
                    }
                    func = cleanFunc(component._scaleFunctions[i]);
                    if (func != null)
                    {
                        component._scaleJEPs[i] = new JEP();
                        component._scaleJEPs[i].addStandardFunctions();
                        component._scaleJEPs[i].addStandardConstants();
                        for (var entry : MolangVars.JEP_VARS.entrySet()) if (func.contains(entry.getKey()))
                            component._scaleJEPs[i].addVariable(entry.getKey(), entry.getValue());
                        component._scaleJEPs[i].parseExpression(func);
                    }
                }
            }

            return component;
        }

        @Override
        public boolean animate(Animation animation, IAnimationHolder holder, IExtendedModelPart part, float partialTick,
                float limbSwing, int tick)
        {
            if (this.hidden) return false;

            boolean animated = false;
            final Vector3 temp = animation._shift.clear();

            float px = 0, py = 0, pz = 0;
            float rx = 0, ry = 0, rz = 0;
            float sx = 1, sy = 1, sz = 1;
            float alpha_scale = 1.0f, red_scale = 1.0f, blue_scale = 1.0f, green_scale = 1.0f;
            float time1;
            float time2;
            MolangVars molangs = holder.getMolangVars();

            time1 = (float) molangs.getAnimTime();
            time2 = (float) molangs.l;

            int aniTick = (int) Math.ceil(time1);

            boolean wasHidden = part.isHidden();

            // First clear these
            dr[0] = dr[1] = dr[2] = 0;
            dx[0] = dx[1] = dx[2] = 0;
            ds[0] = ds[1] = ds[2] = 1;
            dc[0] = dc[1] = dc[2] = 1;

            // Marker for if any were hidden
            boolean any_hidden = false;

            boolean limb = part instanceof Part p && p.isOverridenLimb;

            // Now we run through the components per channel

            CHANNEL channel = CHANNEL.ROT;
            // Rotation set
            rots:
            {
                var animChannel = channels.get(channel.ordinal());
                if (animChannel == null) break rots;
                float t1 = time1, t2 = time2;
                if (animation.loops)
                {
                    float l = animChannel.length();
                    if (l > 1)
                    {
                        t1 = time1 % l;
                        t2 = time2 % l;
                    }
                }
                else
                {
                    t1 = Math.min(time1, animChannel.length());
                    t2 = Math.min(time2, animChannel.length());
                }
                AnimationComponent component = getNext(t1, t2, animation.loops, animChannel);
                if (component == null) break rots;
                animated = true;
                float time = component.limbBased || limb ? t2 : t1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));

                any_hidden |= component.hidden;

                // Start by checking JEP components to the animation
                for (int i = 0; i < 3; i++) if (component._rotJEPs[i] != null)
                {
                    molangs.updateJEP(component._rotJEPs[i], t1, t2);
                    dr[i] = (float) component._rotJEPs[i].getValue() * component._rotFuncScale[i];
                }

                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final float length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;

                rx += Math.fma(component.rotChange[0], ratio, component.rotOffset[0]);
                ry += Math.fma(component.rotChange[1], ratio, component.rotOffset[1]);
                rz += Math.fma(component.rotChange[2], ratio, component.rotOffset[2]);
            }

            channel = CHANNEL.POS;
            // Position set
            pos:
            {
                var animChannel = channels.get(channel.ordinal());
                if (animChannel == null) break pos;
                float t1 = time1, t2 = time2;
                if (animation.loops)
                {
                    float l = animChannel.length();
                    if (l > 1)
                    {
                        t1 = time1 % l;
                        t2 = time2 % l;
                    }
                }
                else
                {
                    t1 = Math.min(time1, animChannel.length());
                    t2 = Math.min(time2, animChannel.length());
                }
                AnimationComponent component = getNext(t1, t2, animation.loops, animChannel);
                if (component == null) break pos;
                animated = true;
                float time = component.limbBased || limb ? t2 : t1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));

                any_hidden |= component.hidden;

                // Start by checking JEP components to the animation
                for (int i = 0; i < 3; i++) if (component._posJEPs[i] != null)
                {
                    molangs.updateJEP(component._posJEPs[i], t1, t2);
                    dx[i] = (float) component._posJEPs[i].getValue() * component._posFuncScale[i];
                }

                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final float length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;

                px += Math.fma(component.posChange[0], ratio, component.posOffset[0]);
                py += Math.fma(component.posChange[1], ratio, component.posOffset[1]);
                pz += Math.fma(component.posChange[2], ratio, component.posOffset[2]);
            }

            channel = CHANNEL.SCALE;
            // scale set
            scales:
            {
                var animChannel = channels.get(channel.ordinal());
                if (animChannel == null) break scales;
                float t1 = time1, t2 = time2;
                if (animation.loops)
                {
                    float l = animChannel.length();
                    if (l > 1)
                    {
                        t1 = time1 % l;
                        t2 = time2 % l;
                    }
                }
                else
                {
                    t1 = Math.min(time1, animChannel.length());
                    t2 = Math.min(time2, animChannel.length());
                }
                AnimationComponent component = getNext(t1, t2, animation.loops, animChannel);
                if (component == null) break scales;
                animated = true;
                float time = component.limbBased || limb ? t2 : t1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));

                any_hidden |= component.hidden;

                // Start by checking JEP components to the animation
                for (int i = 0; i < 3; i++) if (component._scaleJEPs[i] != null)
                {
                    molangs.updateJEP(component._scaleJEPs[i], t1, t2);
                    ds[i] = (float) component._scaleJEPs[i].getValue();
                }

                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final float length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;

                sx *= Math.fma(component.scaleChange[0], ratio, component.scaleOffset[0]);
                sy *= Math.fma(component.scaleChange[1], ratio, component.scaleOffset[1]);
                sz *= Math.fma(component.scaleChange[2], ratio, component.scaleOffset[2]);
            }

            channel = CHANNEL.OPACITY;
            // opacity set
            opacity:
            {
                var animChannel = channels.get(channel.ordinal());
                if (animChannel == null) break opacity;
                float t1 = time1, t2 = time2;
                if (animation.loops)
                {
                    float l = animChannel.length();
                    if (l > 1)
                    {
                        t1 = time1 % l;
                        t2 = time2 % l;
                    }
                }
                else
                {
                    t1 = Math.min(time1, animChannel.length());
                    t2 = Math.min(time2, animChannel.length());
                }
                AnimationComponent component = getNext(t1, t2, animation.loops, animChannel);
                if (component == null) break opacity;
                animated = true;
                float time = component.limbBased || limb ? t2 : t1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));

                any_hidden |= component.hidden;

                if (component._opacJEP != null)
                {
                    molangs.updateJEP(component._opacJEP, t1, t2);
                    alpha_scale *= component._opacJEP.getValue();
                }

                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final float length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;

                alpha_scale *= Math.fma(component.opacityChange, ratio, component.opacityOffset);
            }

            channel = CHANNEL.COLOUR;
            // colour set
            colour:
            {
                var animChannel = channels.get(channel.ordinal());
                if (animChannel == null) break colour;
                float t1 = time1, t2 = time2;
                if (animation.loops)
                {
                    float l = animChannel.length();
                    if (l > 1)
                    {
                        t1 = time1 % l;
                        t2 = time2 % l;
                    }
                }
                else
                {
                    t1 = Math.min(time1, animChannel.length());
                    t2 = Math.min(time2, animChannel.length());
                }
                AnimationComponent component = getNext(t1, t2, animation.loops, animChannel);
                if (component == null) break colour;
                animated = true;
                float time = component.limbBased || limb ? t2 : t1;
                aniTick = Math.max(aniTick, (int) Math.ceil(time));

                any_hidden |= component.hidden;

                // Start by checking JEP components to the animation
                for (int i = 0; i < 3; i++) if (component._colJEPs[i] != null)
                {
                    molangs.updateJEP(component._colJEPs[i], t1, t2);
                    dc[i] = (float) component._colJEPs[i].getValue();
                }

                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final float length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;

                red_scale *= Math.fma(component.colChange[0], ratio, component.colOffset[0]);
                green_scale *= Math.fma(component.colChange[1], ratio, component.colOffset[1]);
                blue_scale *= Math.fma(component.colChange[2], ratio, component.colOffset[2]);

                red_scale *= dc[0];
                green_scale *= dc[1];
                blue_scale *= dc[2];
            }

            channel = CHANNEL.HIDDEN;
            // colour set
            hidden:
            {
                var animChannel = channels.get(channel.ordinal());
                if (animChannel == null) break hidden;
                float t1 = time1, t2 = time2;
                if (animation.loops)
                {
                    float l = animChannel.length();
                    if (l > 1)
                    {
                        t1 = time1 % l;
                        t2 = time2 % l;
                    }
                }
                else
                {
                    t1 = Math.min(time1, animChannel.length());
                    t2 = Math.min(time2, animChannel.length());
                }
                AnimationComponent component = getNext(t1, t2, animation.loops, animChannel);
                if (component == null) break hidden;
                any_hidden = true;
            }

            // Apply hidden like this so last hidden state is kept
            if (wasHidden != any_hidden) part.setHidden(any_hidden);
            part.setColorScales(red_scale, green_scale, blue_scale, alpha_scale);

            if (animated)
            {
                temp.set(px, py, pz);

                rx += dr[0];
                ry += dr[1];
                rz += dr[2];

                temp.addTo(dx[0], dx[1], dx[2]);

                sx *= ds[0];
                sy *= ds[1];
                sz *= ds[2];

                part.setPreTranslations(temp);
                part.setPreScale(temp.set(sx, sy, sz));
                part.setAnimAngles(rx, ry, rz);
            }

            return animated;
        }

        @Override
        public float getLength()
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
                Set<CHANNEL> our_channels = this.channelSet;
                Set<CHANNEL> other_channels = anim.channelSet;
                return Sets.intersection(our_channels, other_channels).isEmpty();
            }
            return IAnimator.super.conflicts(other);
        }
    }
}
