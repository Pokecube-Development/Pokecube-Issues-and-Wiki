package thut.core.client.render.animation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import thut.api.ThutCaps;
import thut.api.entity.IMobColourable;
import thut.api.entity.IShearable;
import thut.api.entity.ShearableCaps;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;

public class AnimationChanger implements IAnimationChanger
{

    List<IAnimationChanger>  children   = Lists.newArrayList();
    /** These parts can be sheared off. */
    public final Set<String> shearables = Sets.newHashSet();
    /** These parts are dyed based on the specialInfo of the pokemob; */
    public final Set<String> dyeables   = Sets.newHashSet();

    /**
     * These parts get a specific colour offset from the default colour of the
     * specialInfo.
     */
    public final Map<String, Function<Integer, Integer>> colourOffsets = Maps.newHashMap();
    /** This is a set of valid offsets for worn items on the pokemob. */
    public final Map<String, WornOffsets>                wornOffsets   = Maps.newHashMap();

    /**
     * This is a cache of which parts have been checked for being a
     * wildcard.
     */
    private final Set<String> checkWildCard = Sets.newHashSet();

    IAnimationHolder currentHolder = null;

    public AnimationChanger()
    {
    }

    @Override
    public void reset()
    {
        this.children.clear();
        this.shearables.clear();
        this.dyeables.clear();
        this.colourOffsets.clear();
        this.wornOffsets.clear();
        this.checkWildCard.clear();
    }

    @Override
    public void addChild(final IAnimationChanger animationRandomizer)
    {
        this.children.add(animationRandomizer);
    }

    private void checkWildCard(final String partIdentifier)
    {
        if (!this.checkWildCard.contains(partIdentifier))
        {
            this.checkWildCard.add(partIdentifier);
            for (final String s : this.dyeables)
                if (s.startsWith("*") && partIdentifier.matches(s.substring(1)))
                {
                    this.dyeables.add(partIdentifier);
                    if (this.colourOffsets.containsKey(s)) this.colourOffsets.put(partIdentifier, this.colourOffsets
                            .get(s));
                    break;
                }
            for (final String s : this.shearables)
                if (s.startsWith("*") && partIdentifier.matches(s.substring(1)))
                {
                    this.dyeables.add(partIdentifier);
                    break;
                }
        }
    }

    @Override
    public boolean modifyColourForPart(final String partIdentifier, final Entity entity, final int[] rgba)
    {
        this.checkWildCard(partIdentifier);
        for (final IAnimationChanger child : this.children)
            if (child.modifyColourForPart(partIdentifier, entity, rgba)) return true;
        final int rgb = this.getColourForPart(partIdentifier, entity);
        final int a = rgb >> 24 & 255;
        final int r = rgb >> 16 & 255;
        final int g = rgb >> 8 & 255;
        final int b = rgb & 255;
        rgba[0] = r;
        rgba[1] = g;
        rgba[2] = b;
        rgba[3] = a;
        return true;
    }

    public int getColourForPart(final String partIdentifier, final Entity entity)
    {
        this.checkWildCard(partIdentifier);
        int rgba = 0xFFFFFFFF;
        final IMobColourable pokemob = entity.getCapability(ThutCaps.COLOURABLE).orElse(null);
        if (pokemob == null) return rgba;
        rgba = 0xFF000000;
        if (this.dyeables.contains(partIdentifier))
        {
            final Function<Integer, Integer> offset = this.colourOffsets.get(partIdentifier);
            int colour = pokemob.getDyeColour() & 15;
            if (offset != null) colour = offset.apply(colour);
            rgba += DyeColor.byId(colour).textColor;
            return rgba;
        }
        final int[] arr = pokemob.getRGBA();
        rgba = (arr[3] & 0xFF) << 24 | (arr[0] & 0xFF) << 16 | (arr[1] & 0xFF) << 8 | (arr[2] & 0xFF) << 0;
        return rgba;
    }

    @Override
    public WornOffsets getOffsets(final String part)
    {
        return this.wornOffsets.get(part);
    }

    @Override
    public void init(final Set<Animation> existingAnimations)
    {
        for (final IAnimationChanger child : this.children)
            child.init(existingAnimations);
    }

    @Override
    public boolean hasAnimation(final String phase)
    {
        for (final IAnimationChanger child : this.children)
            if (child.hasAnimation(phase)) return true;
        return IAnimationChanger.super.hasAnimation(phase);
    }

    @Override
    public boolean isPartHidden(final String part, final Entity entity, final boolean default_)
    {
        this.checkWildCard(part);
        for (final IAnimationChanger child : this.children)
            if (child.isPartHidden(part, entity, default_)) return true;
        final IShearable shear = ShearableCaps.get(entity);
        if (this.shearables.contains(part) && shear != null) return shear.isSheared();
        return default_;
    }

    @Override
    public boolean getAlternates(final List<String> toFill, final Set<String> options, final Entity mob,
            final String phase)
    {
        boolean ret = false;
        for (final IAnimationChanger child : this.children)
            ret = child.getAlternates(toFill, options, mob, phase) || ret;
        if (ret) return true;
        return IAnimationChanger.super.getAlternates(toFill, options, mob, phase);
    }

    @Override
    public void parseDyeables(final Set<String> set)
    {
        this.dyeables.addAll(set);
    }

    @Override
    public void parseShearables(final Set<String> set)
    {
        this.shearables.addAll(set);
    }

    @Override
    public void parseWornOffsets(final Map<String, WornOffsets> map)
    {
        this.wornOffsets.putAll(map);
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
        for (final IAnimationChanger child : this.children)
            child.setAnimationHolder(holder);
    }

}
