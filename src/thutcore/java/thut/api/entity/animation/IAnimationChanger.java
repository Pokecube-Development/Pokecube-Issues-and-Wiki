package thut.api.entity.animation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.core.common.ThutCore;

public interface IAnimationChanger
{
    public record WornOffsets(String parent, Vector3f offset, Vector3f scale, Vector3f angles)
    {
        public WornOffsets(final String parent, final Vector3f offset, final Vector3f scale, final Vector3f angles)
        {
            this.scale = scale;
            this.angles = angles;
            this.offset = offset;
            this.parent = ThutCore.trim(parent);
        }
    }

    void addChild(IAnimationChanger animationRandomizer);

    default boolean modifyColourForPart(final String partIdentifier, final Entity entity, final int[] rgba)
    {
        return false;
    }

    default boolean hasAnimation(final String phase)
    {
        return this.getAnimations().containsKey(phase);
    }

    default void reset()
    {
    }

    Map<String, List<Animation>> getAnimations();

    @Nullable
    default WornOffsets getOffsets(String part){return null;}

    void init(Collection<Animation> anims);

    default boolean isPartHidden(final String part, final Entity entity, final boolean default_)
    {
        return default_;
    }

    default boolean getAlternates(final List<String> toFill, final Entity mob,
            final String phase)
    {
        if (getAnimations().containsKey(phase))
        {
            toFill.add(phase);
            return true;
        }
        return false;
    }

    void setAnimationHolder(IAnimationHolder holder);

    IAnimationHolder getAnimationHolder();

    void parseDyeables(Set<String> set);

    void parseShearables(Set<String> set);

    void parseWornOffsets(Map<String, WornOffsets> map);
}
