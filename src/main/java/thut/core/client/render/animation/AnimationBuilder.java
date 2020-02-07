package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.common.ThutCore;

public class AnimationBuilder
{
    private static void addTo(final Animation animation, final int priority, final String part,
            final ArrayList<AnimationComponent> parts)
    {
        if (animation.sets.containsKey(part) && animation.priority > priority)
            System.err.println("Already have " + part + ", Skipping.");
        else animation.sets.put(part, parts);
    }

    /** Constructs a new Animation, and assigns components based on the
     * definitions in the XML node.
     *
     * @param node
     * @param renamer
     * @return */
    public static Animation build(final Node node, @Nullable final IPartRenamer renamer)
    {
        Animation ret = null;
        if (node.getAttributes().getNamedItem("type") == null) return null;
        String animName = node.getAttributes().getNamedItem("type").getNodeValue();
        animName = ThutCore.trim(animName);
        ThutCore.LOGGER.debug("Generating animation: " + animName);
        ret = new Animation();
        ret.name = animName;
        ret.loops = true;
        if (node.getAttributes().getNamedItem("loops") != null)
            ret.loops = Boolean.parseBoolean(node.getAttributes().getNamedItem("loops").getNodeValue());

        final NodeList parts = node.getChildNodes();
        Node temp;
        for (int i = 0; i < parts.getLength(); i++)
        {
            final Node part = parts.item(i);
            if (part.getNodeName().equals("part"))
            {
                final NodeList components = part.getChildNodes();
                String partName = part.getAttributes().getNamedItem("name").getNodeValue();
                if (renamer != null)
                {
                    final String[] names = { partName };
                    renamer.convertToIdents(names);
                    partName = names[0];
                }
                partName = ThutCore.trim(partName);
                final ArrayList<AnimationComponent> set = Lists.newArrayList();
                for (int j = 0; j < components.getLength(); j++)
                {
                    final Node component = components.item(j);
                    if (component.getNodeName().equals("component"))
                    {
                        final AnimationComponent comp = new AnimationComponent();
                        if ((temp = component.getAttributes().getNamedItem("name")) != null)
                            comp.name = temp.getNodeValue();
                        if ((temp = component.getAttributes().getNamedItem("rotChange")) != null)
                        {
                            final String[] vals = temp.getNodeValue().split(",");
                            comp.rotChange[0] = Double.parseDouble(vals[0]);
                            comp.rotChange[1] = Double.parseDouble(vals[1]);
                            comp.rotChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("posChange")) != null)
                        {
                            final String[] vals = temp.getNodeValue().split(",");
                            comp.posChange[0] = Double.parseDouble(vals[0]);
                            comp.posChange[1] = Double.parseDouble(vals[1]);
                            comp.posChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("scaleChange")) != null)
                        {
                            final String[] vals = temp.getNodeValue().split(",");
                            comp.scaleChange[0] = Double.parseDouble(vals[0]);
                            comp.scaleChange[1] = Double.parseDouble(vals[1]);
                            comp.scaleChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("rotOffset")) != null)
                        {
                            final String[] vals = temp.getNodeValue().split(",");
                            comp.rotOffset[0] = Double.parseDouble(vals[0]);
                            comp.rotOffset[1] = Double.parseDouble(vals[1]);
                            comp.rotOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("posOffset")) != null)
                        {
                            final String[] vals = temp.getNodeValue().split(",");
                            comp.posOffset[0] = Double.parseDouble(vals[0]);
                            comp.posOffset[1] = Double.parseDouble(vals[1]);
                            comp.posOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("scaleOffset")) != null)
                        {
                            final String[] vals = temp.getNodeValue().split(",");
                            comp.scaleOffset[0] = Double.parseDouble(vals[0]);
                            comp.scaleOffset[1] = Double.parseDouble(vals[1]);
                            comp.scaleOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("length")) != null)
                            comp.length = Integer.parseInt(temp.getNodeValue());
                        if ((temp = component.getAttributes().getNamedItem("startKey")) != null)
                            comp.startKey = Integer.parseInt(temp.getNodeValue());
                        if ((temp = component.getAttributes().getNamedItem("opacityChange")) != null)
                            comp.opacityChange = Double.parseDouble(temp.getNodeValue());
                        if ((temp = component.getAttributes().getNamedItem("opacityOffset")) != null)
                            comp.opacityOffset = Double.parseDouble(temp.getNodeValue());
                        if ((temp = component.getAttributes().getNamedItem("hidden")) != null)
                            comp.hidden = Boolean.parseBoolean(temp.getNodeValue());
                        set.add(comp);
                    }
                }
                if (!set.isEmpty()) ret.sets.put(partName, set);
            }
        }
        return ret;
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
