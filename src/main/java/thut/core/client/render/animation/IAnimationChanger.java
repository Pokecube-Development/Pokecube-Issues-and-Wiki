package thut.core.client.render.animation;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import thut.api.maths.Vector3;

public interface IAnimationChanger
{
    public static class WornOffsets
    {
        public final String  parent;
        public final Vector3 offset;
        public final Vector3 scale;
        public final Vector3 angles;

        public WornOffsets(String parent, Vector3 offset, Vector3 scale, Vector3 angles)
        {
            this.scale = scale;
            this.angles = angles;
            this.offset = offset;
            this.parent = parent;
        }
    }

    void addChild(IAnimationChanger animationRandomizer);

    int getColourForPart(String partIdentifier, Entity entity, int default_);

    @Nullable
    WornOffsets getOffsets(String part);

    void init(Set<Animation> anims);

    boolean isPartHidden(String part, Entity entity, boolean default_);

    String modifyAnimation(MobEntity entity, float partialTicks, String phase);

    void parseDyeables(Set<String> set);

    void parseShearables(Set<String> set);

    void parseWornOffsets(Map<String, WornOffsets> map);
}
