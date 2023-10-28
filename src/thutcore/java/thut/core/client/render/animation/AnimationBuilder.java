package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animation.IPartRenamer;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators;
import thut.api.entity.animation.Animators.IAnimator;
import thut.api.entity.animation.Animators.KeyframeAnimator;
import thut.core.client.render.animation.AnimationXML.Component;
import thut.core.client.render.animation.AnimationXML.Part;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.common.ThutCore;

public class AnimationBuilder
{
    private static void addTo(final Animation animation, final int priority, final String part, final IAnimator parts)
    {
        boolean conflicts = animation.sets.containsKey(part);
        conflicts = conflicts && animation.sets.get(part).conflicts(parts);
        if (conflicts && animation.priority > priority) ThutCore.LOGGER.debug("Already have " + part + ", Skipping.");
        else animation.sets.put(part, parts);
    }

    /**
     * Constructs a new Animation, and assigns components based on the
     * definitions in the XML node.
     *
     * @param node
     * @param set2
     * @param renamer
     * @return
     */
    public static Animation build(final Phase node, final Set<String> valid_names, @Nullable final IPartRenamer renamer)
    {
        Animation ret = null;
        if (node.type == null) return null;
        final String animName = ThutCore.trim(node.type);

        ret = new Animation();
        ret.name = animName;
        ret.loops = true;
        if (AnimationBuilder.get(node, "loops") != null)
            ret.loops = Boolean.parseBoolean(AnimationBuilder.get(node, "loops"));

        for (final Part part : node.parts)
        {
            final boolean regex = part.name.startsWith("*");
            final List<String> partNames = Lists.newArrayList();
            if (regex)
            {
                final String key = part.name.substring(1).toLowerCase(Locale.ROOT).replace(" ", "_");
                for (final String s : valid_names) if (s.matches(key)) partNames.add(s);
            }
            else
            {
                String[] subParts = part.name.split(":");
                for (String s : subParts)
                {
                    String partName = ThutCore.trim(s);
                    if (renamer != null)
                    {
                        final String[] names =
                        { partName };
                        renamer.convertToIdents(names);
                        partName = names[0];
                    }
                    partNames.add(partName);
                }
            }

            for (final String partName : partNames)
            {
                final ArrayList<AnimationComponent> set = Lists.newArrayList();
                for (final Component component : part.components)
                {
                    final AnimationComponent comp = new AnimationComponent();

                    String[] rots = comp._rotFunctions;
                    String[] pos = comp._posFunctions;
                    String[] col = comp._colFunctions;
                    String[] scale = comp._scaleFunctions;

                    if (!component.rotFuncs.isBlank()) Animators.fillJEPs(rots, component.rotFuncs);
                    if (!component.posFuncs.isBlank()) Animators.fillJEPs(pos, component.posFuncs);
                    if (!component.colFuncs.isBlank()) Animators.fillJEPs(col, component.colFuncs);
                    if (!component.scaleFuncs.isBlank()) Animators.fillJEPs(scale, component.scaleFuncs);
                    if (!component.opacFuncs.isBlank())
                    {
                        var func = component.opacFuncs;
                        comp._opacFunction = func;
                    }

                    if (component.name != null) comp.name = component.name;
                    if (component.rotChange != null)
                    {
                        final String[] vals = component.rotChange.split(",");
                        comp.rotChange[0] = Double.parseDouble(vals[0]);
                        comp.rotChange[1] = Double.parseDouble(vals[1]);
                        comp.rotChange[2] = Double.parseDouble(vals[2]);
                    }
                    if (component.posChange != null)
                    {
                        final String[] vals = component.posChange.split(",");
                        comp.posChange[0] = Double.parseDouble(vals[0]);
                        comp.posChange[1] = Double.parseDouble(vals[1]);
                        comp.posChange[2] = Double.parseDouble(vals[2]);
                    }
                    if (component.colChange != null)
                    {
                        final String[] vals = component.colChange.split(",");
                        comp.colChange[0] = Double.parseDouble(vals[0]);
                        comp.colChange[1] = Double.parseDouble(vals[1]);
                        comp.colChange[2] = Double.parseDouble(vals[2]);
                    }
                    if (component.scaleChange != null)
                    {
                        final String[] vals = component.scaleChange.split(",");
                        comp.scaleChange[0] = Double.parseDouble(vals[0]);
                        comp.scaleChange[1] = Double.parseDouble(vals[1]);
                        comp.scaleChange[2] = Double.parseDouble(vals[2]);
                    }
                    if (component.rotOffset != null)
                    {
                        final String[] vals = component.rotOffset.split(",");
                        comp.rotOffset[0] = Double.parseDouble(vals[0]);
                        comp.rotOffset[1] = Double.parseDouble(vals[1]);
                        comp.rotOffset[2] = Double.parseDouble(vals[2]);
                    }
                    if (component.posOffset != null)
                    {
                        final String[] vals = component.posOffset.split(",");
                        comp.posOffset[0] = Double.parseDouble(vals[0]);
                        comp.posOffset[1] = Double.parseDouble(vals[1]);
                        comp.posOffset[2] = Double.parseDouble(vals[2]);
                    }
                    if (component.colOffset != null)
                    {
                        final String[] vals = component.colOffset.split(",");
                        comp.colOffset[0] = Double.parseDouble(vals[0]);
                        comp.colOffset[1] = Double.parseDouble(vals[1]);
                        comp.colOffset[2] = Double.parseDouble(vals[2]);
                    }
                    if (component.scaleOffset != null)
                    {
                        final String[] vals = component.scaleOffset.split(",");
                        comp.scaleOffset[0] = Double.parseDouble(vals[0]);
                        comp.scaleOffset[1] = Double.parseDouble(vals[1]);
                        comp.scaleOffset[2] = Double.parseDouble(vals[2]);
                    }
                    comp.length = component.length;
                    comp.startKey = component.startKey;
                    comp.opacityChange = component.opacityChange;
                    comp.opacityOffset = component.opacityOffset;
                    comp.hidden = component.hidden;
                    set.add(comp);
                }
                if (!set.isEmpty()) ret.sets.put(partName, new KeyframeAnimator(set));
            }
        }
        return ret;
    }

    private static String get(final Phase node, final String string)
    {
        return node.values.get(new QName(string));
    }

    private static Animation mergeAnimations(final List<Animation> list)
    {
        if (list.isEmpty()) return null;
        final Animation newAnim = new Animation();
        var old = list.get(0);
        newAnim.name = old.name;
        newAnim.identifier = old.identifier;
        newAnim.loops = old.loops;
        newAnim.priority = old.priority;
        newAnim.holdWhenDone = old.holdWhenDone;
        for (final Animation anim : list) for (final String part : anim.sets.keySet())
            AnimationBuilder.addTo(newAnim, anim.priority, part, anim.sets.get(part));
        return newAnim;
    }

    public static void processAnimations(final List<Animation> list)
    {
        final List<Animation> oldList = Lists.newArrayList(list);
        list.clear();
        list.add(AnimationBuilder.mergeAnimations(oldList));
    }
}
