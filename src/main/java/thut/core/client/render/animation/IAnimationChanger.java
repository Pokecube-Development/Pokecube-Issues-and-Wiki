package thut.core.client.render.animation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import thut.api.maths.Vector3;

public interface IAnimationChanger
{
    public static class WornOffsets
    {
        public final String  parent;
        public final Vector3 offset;
        public final Vector3 scale;
        public final Vector3 angles;

        public WornOffsets(final String parent, final Vector3 offset, final Vector3 scale, final Vector3 angles)
        {
            this.scale = scale;
            this.angles = angles;
            this.offset = offset;
            this.parent = parent;
        }
    }

    void addChild(IAnimationChanger animationRandomizer);

    default int getColourForPart(final String partIdentifier, final Entity entity, final int default_)
    {
        return default_;
    }

    default boolean hasAnimation(final String phase)
    {
        return false;
    }

    default void reset()
    {
    }

    @Nullable
    WornOffsets getOffsets(String part);

    void init(Set<Animation> anims);

    default boolean isPartHidden(final String part, final Entity entity, final boolean default_)
    {
        return default_;
    }

    default boolean getAlternates(final List<String> toFill, final Set<String> options, final Entity mob,
            final String phase)
    {
        if (options.contains(phase)) toFill.add(phase);
        return true;
    }

    void parseDyeables(Set<String> set);

    void parseShearables(Set<String> set);

    void parseWornOffsets(Map<String, WornOffsets> map);
}
