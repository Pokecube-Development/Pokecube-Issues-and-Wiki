package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.client.render.animation.AnimationXML.Component;
import thut.core.client.render.animation.AnimationXML.Part;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.common.ThutCore;

public class AnimationBuilder
{
    private static void addTo(final Animation animation, final int priority, final String part,
            final ArrayList<AnimationComponent> parts)
    {
        if (animation.sets.containsKey(part) && animation.priority > priority)
            ThutCore.LOGGER.warn("Already have " + part + ", Skipping.");
        else animation.sets.put(part, parts);
    }

    /** Constructs a new Animation, and assigns components based on the
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
        if (AnimationBuilder.get(node, "loops") != null) ret.loops = Boolean.parseBoolean(AnimationBuilder.get(node,
                "loops"));

        for (final Part part : node.parts)
        {
            final boolean regex = part.name.startsWith("*");
            final List<String> partNames = Lists.newArrayList();
            if (regex)
            {
                final String key = part.name.substring(1).toLowerCase(Locale.ROOT).replace(" ", "_");
                for (final String s : valid_names)
                    if (s.matches(key)) partNames.add(s);
            }
            else
            {
                String partName = ThutCore.trim(part.name);
                if (renamer != null)
                {
                    final String[] names = { partName };
                    renamer.convertToIdents(names);
                    partName = names[0];
                }
                partNames.add(partName);
            }
            for (final String partName : partNames)
            {
                final ArrayList<AnimationComponent> set = Lists.newArrayList();
                for (final Component component : part.components)
                {
                    final AnimationComponent comp = new AnimationComponent();
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
                if (!set.isEmpty()) ret.sets.put(partName, set);
            }
        }
        return ret;
    }

    private static String get(final Phase node, final String string)
    {
        return node.values.get(new QName(string));
    }

    private static int length(final List<AnimationComponent> comps)
    {
        int length = 0;
        for (final AnimationComponent comp : comps)
            length = Math.max(length, comp.startKey + comp.length);
        return length;
    }

    private static Animation mergeAnimations(final List<Animation> list)
    {
        if (list.isEmpty()) return null;
        final Animation newAnim = new Animation();
        newAnim.name = list.get(0).name;
        newAnim.identifier = list.get(0).identifier;
        newAnim.loops = list.get(0).loops;
        newAnim.priority = list.get(0).priority;
        for (final Animation anim : list)
            for (final String part : anim.sets.keySet())
                AnimationBuilder.addTo(newAnim, anim.priority, part, anim.sets.get(part));
        return newAnim;
    }

    public static void processAnimations(final List<Animation> list)
    {
        final List<Animation> oldList = Lists.newArrayList(list);
        final Map<Integer, List<Animation>> splitAnims = Maps.newHashMap();
        for (final Animation anim : oldList)
            AnimationBuilder.splitAnimation(anim, splitAnims);
        list.clear();
        for (final List<Animation> split : splitAnims.values())
            list.add(AnimationBuilder.mergeAnimations(split));
    }

    private static void splitAnimation(final Animation animIn, final Map<Integer, List<Animation>> fill)
    {
        for (final Entry<String, ArrayList<AnimationComponent>> entry : animIn.sets.entrySet())
        {
            final String key = entry.getKey();
            final ArrayList<AnimationComponent> comps = entry.getValue();
            final int length = AnimationBuilder.length(comps);
            List<Animation> anims = fill.get(length);
            if (anims == null) fill.put(length, anims = Lists.newArrayList());
            final Animation newAnim = new Animation();
            newAnim.name = animIn.name;
            newAnim.identifier = animIn.identifier;
            newAnim.loops = animIn.loops;
            newAnim.priority = animIn.priority;
            newAnim.sets.put(key, comps);
            anims.add(newAnim);
        }
    }
}
