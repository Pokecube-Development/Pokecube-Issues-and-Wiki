package thut.core.client.render.animation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.DyeColor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import thut.api.entity.IMobColourable;
import thut.api.entity.IShearable;
import thut.api.entity.ShearableCaps;

public class AnimationChanger implements IAnimationChanger
{
    @CapabilityInject(IMobColourable.class)
    public static final Capability<IMobColourable>       CAPABILITY    = null;
    List<IAnimationChanger>                              children      = Lists.newArrayList();
    /** These parts can be sheared off. */
    public final Set<String>                             shearables    = Sets.newHashSet();
    /** These parts are dyed based on the specialInfo of the pokemob; */
    public final Set<String>                             dyeables      = Sets.newHashSet();
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
    private final Set<String>                            checkWildCard = Sets.newHashSet();

    public AnimationChanger()
    {
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
    public int getColourForPart(final String partIdentifier, final Entity entity, final int default_)
    {
        this.checkWildCard(partIdentifier);
        dye:
        if (this.dyeables.contains(partIdentifier))
        {
            int rgba = 0xFF000000;
            final IMobColourable pokemob = entity.getCapability(AnimationChanger.CAPABILITY).orElse(null);
            if (pokemob == null) break dye;
            final Function<Integer, Integer> offset = this.colourOffsets.get(partIdentifier);
            int colour = pokemob.getDyeColour() & 15;
            if (offset != null) colour = offset.apply(colour);
            rgba += DyeColor.byId(colour).textColor;
            return rgba;
        }
        for (final IAnimationChanger child : this.children)
        {
            final int var = child.getColourForPart(partIdentifier, entity, default_);
            if (var != default_) return var;
        }
        return default_;
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
    public String modifyAnimation(final MobEntity entity, final float partialTicks, final String phase)
    {
        for (final IAnimationChanger child : this.children)
        {
            final String mod = child.modifyAnimation(entity, partialTicks, phase);
            if (!phase.equals(mod)) return mod;
        }
        return phase;
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

}
