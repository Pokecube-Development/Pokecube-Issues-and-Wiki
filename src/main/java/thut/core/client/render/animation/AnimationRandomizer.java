package thut.core.client.render.animation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import thut.core.common.ThutCore;

public class AnimationRandomizer implements IAnimationChanger
{
    private static class AnimationSet
    {
        final RandomAnimation anim;
        int                   set;

        public AnimationSet(RandomAnimation anim)
        {
            this.anim = anim;
        }

    }

    private static class LoadedAnimSet
    {
        String name;
        double chance;
    }

    private static class RandomAnimation
    {
        final String name;
        double       chance   = 0.005;
        int          duration = 0;

        public RandomAnimation(Animation animation, double chance)
        {
            this.chance = chance;
            this.name = animation.name;
            this.duration = animation.getLength();
        }
    }

    // TODO way to clean this up.
    Map<Integer, AnimationSet>         running    = Maps.newHashMap();

    Map<String, List<RandomAnimation>> sets       = Maps.newHashMap();

    Map<String, Set<LoadedAnimSet>>    loadedSets = Maps.newHashMap();

    public AnimationRandomizer(Node node)
    {
        final NodeList parts = node.getChildNodes();
        for (int i = 0; i < parts.getLength(); i++)
        {
            final Node part = parts.item(i);
            if (part.getAttributes() == null) continue;
            String parent = part.getAttributes().getNamedItem("parent").getNodeValue();
            String name = part.getAttributes().getNamedItem("name").getNodeValue();
            name = ThutCore.trim(name);
            parent = ThutCore.trim(parent);
            final double chance = Double.parseDouble(part.getAttributes().getNamedItem("chance").getNodeValue());
            final LoadedAnimSet set = new LoadedAnimSet();
            set.chance = chance;
            set.name = name;
            Set<LoadedAnimSet> sets = this.loadedSets.get(parent);
            if (sets == null) this.loadedSets.put(parent, sets = Sets.newHashSet());
            sets.add(set);
        }
    }

    private void addAnimationSet(Animation animation, double chance, String parent)
    {
        List<RandomAnimation> anims = this.sets.get(parent);
        if (anims == null) this.sets.put(parent, anims = Lists.newArrayList());
        anims.add(new RandomAnimation(animation, chance));
    }

    @Override
    public void addChild(IAnimationChanger randomizer)
    {
        // Nope
    }

    @Override
    public int getColourForPart(String partIdentifier, Entity entity, int default_)
    {
        return default_;
    }

    @Override
    public WornOffsets getOffsets(String part)
    {
        return null;
    }

    @Override
    public void init(Set<Animation> existingAnimations)
    {
        final Set<String> animations = Sets.newHashSet();
        for (final Animation existing : existingAnimations)
            if (this.loadedSets.containsKey(existing.name)) animations.add(existing.name);
        for (final String s : animations)
        {
            final Set<LoadedAnimSet> set = this.loadedSets.get(s);
            for (final LoadedAnimSet loaded : set)
                for (final Animation anim : existingAnimations)
                    if (anim.name.equals(loaded.name))
                    {
                        this.addAnimationSet(anim, loaded.chance, s);
                        break;
                    }
        }
    }

    @Override
    public boolean isPartHidden(String part, Entity entity, boolean default_)
    {
        return default_;
    }

    @Override
    public String modifyAnimation(MobEntity entity, float partialTicks, String phase)
    {
        if (this.running.containsKey(entity.getEntityId()))
        {
            final AnimationSet anim = this.running.get(entity.getEntityId());
            phase = anim.anim.name;
            if (anim.set < entity.ticksExisted) this.running.remove(entity.getEntityId());
            return phase;
        }
        else if (this.sets.containsKey(phase))
        {
            final List<RandomAnimation> set = this.sets.get(phase);
            final int rand = entity.getRNG().nextInt(set.size());
            final RandomAnimation anim = set.get(rand);
            if (Math.random() < anim.chance)
            {
                final AnimationSet aSet = new AnimationSet(anim);
                aSet.set = entity.ticksExisted + aSet.anim.duration;
                this.running.put(entity.getEntityId(), aSet);
                phase = anim.name;
            }
        }
        return phase;
    }

    @Override
    public void parseDyeables(Set<String> set)
    {
        // Nope
    }

    @Override
    public void parseShearables(Set<String> set)
    {
        // Nope
    }

    @Override
    public void parseWornOffsets(Map<String, WornOffsets> map)
    {
        // Nope
    }
}
