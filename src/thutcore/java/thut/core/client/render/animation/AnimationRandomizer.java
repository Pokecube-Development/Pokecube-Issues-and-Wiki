package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.world.entity.Entity;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.core.client.render.animation.AnimationXML.SubAnim;

public class AnimationRandomizer implements IAnimationChanger
{
    private record AnimationSet(RandomAnimation anim)
    {
    }

    private static class LoadedAnimSet
    {
        String name;
        int    weight;
    }

    private static class RandomAnimation
    {
        final String name;

        public RandomAnimation(final Animation animation)
        {
            this.name = animation.name;
        }
    }

    Map<String, List<RandomAnimation>> sets       = new HashMap<>();
    Map<String, Set<LoadedAnimSet>>    loadedSets = new HashMap<>();

    Set<String>              allAnims = new HashSet<>();
    Map<String, Set<String>> reversed = new HashMap<>();

    IAnimationHolder currentHolder = null;

    public AnimationRandomizer(final List<SubAnim> anims)
    {
        for (final SubAnim a : anims)
        {
            this.allAnims.add(a.base);
            this.allAnims.add(a.name);
            Set<LoadedAnimSet> sets = this.loadedSets.computeIfAbsent(a.base, k -> new HashSet<>());
            final LoadedAnimSet set = new LoadedAnimSet();
            set.name = a.name;
            set.weight = a.weight;
            final Set<String> parents = this.reversed.getOrDefault(a.name, new HashSet<>());
            parents.add(a.base);
            this.reversed.put(a.name, parents);
            sets.add(set);
        }
    }

    @Override
    public void reset()
    {
        this.sets.clear();
        this.allAnims.clear();
        this.loadedSets.clear();
        this.reversed.clear();
    }

    private void addAnimationSet(final Animation animation, final String parent)
    {
        List<RandomAnimation> anims = this.sets.computeIfAbsent(parent, k -> new ArrayList<>());
        anims.add(new RandomAnimation(animation));
    }

    @Override
    public void addChild(final IAnimationChanger randomizer)
    {
        // Nope
    }

    @Override
    public boolean hasAnimation(final String phase)
    {
        return this.allAnims.contains(phase);
    }

    @Override
    public void init(final Collection<Animation> existingAnimations)
    {
        final Set<String> animations = new HashSet<>(this.loadedSets.keySet());
        for (final Animation existing : existingAnimations)
            if (this.loadedSets.containsKey(existing.name)) animations.add(existing.name);
        for (final String s : animations)
        {
            final Set<LoadedAnimSet> set = this.loadedSets.get(s);
            for (final LoadedAnimSet loaded : set)
                for (final Animation anim : existingAnimations)
                    if (anim.name.equals(loaded.name))
                    {
                        for (int i = 0; i < loaded.weight; i++)
                            this.addAnimationSet(anim, s);
                        break;
                    }
        }
    }

    @Override
    public boolean getAlternates(final List<String> toFill, final Set<String> options, final Entity mob,
            final String phase)
    {
        if (this.sets.containsKey(phase))
        {
            final IAnimationHolder holder = this.getAnimationHolder();
            if (holder != null && !holder.getPlaying().isEmpty() && holder.getPendingAnimations().equals(phase))
                return true;
            final List<RandomAnimation> set = this.sets.get(phase);
            final int rand = new Random(System.nanoTime()).nextInt(set.size());
            final RandomAnimation anim = set.get(rand);
            final AnimationSet aSet = new AnimationSet(anim);
            toFill.add(aSet.anim.name);
            return true;
        }
        return false;
    }

    @Override
    public void parseDyeables(final Set<String> set)
    {
        // Nope
    }

    @Override
    public void parseShearables(final Set<String> set)
    {
        // Nope
    }

    @Override
    public void parseWornOffsets(final Map<String, WornOffsets> map)
    {
        // Nope
    }

    @Override
    public IAnimationHolder getAnimationHolder()
    {
        return this.currentHolder;
    }

    @Override
    public void setAnimationHolder(final IAnimationHolder holder)
    {
        this.currentHolder = holder;
    }
}
