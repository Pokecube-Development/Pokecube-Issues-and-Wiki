package thut.api.entity.multipart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated;
import thut.core.common.network.PartSync;

public interface IMultpart<T extends GenericPartEntity<E>, E extends Entity>
{
    public static class Holder<T extends GenericPartEntity<?>>
    {

        public List<T> allParts;
        public List<T> parts;

        public Vector3f r = new Vector3f();
        public Matrix4f transform = new Matrix4f();
        public String effective_pose = "";

        public int tick = -1, animTick=-1;

        public void clear()
        {
            parts = new ArrayList<>();
        }
    }

    public static record PartHolder<E extends GenericPartEntity<?>> (List<E> allParts, Map<String, List<E>> partMap,
            Holder<E> holder)
    {
        public List<E> getParts()
        {
            return holder.parts;
        }

        public void setParts(List<E> parts)
        {
            holder.parts = parts;
        }

        public void clear()
        {
            allParts.clear();
            partMap.clear();
            holder.clear();
        }
    }

    default List<T> getAllParts()
    {
        return getHolder().allParts();
    }

    default List<T> getUseParts()
    {
        return getHolder().getParts();
    }

    PartHolder<T> getHolder();

    void initParts();

    /**
     * This is not "self" as forge used that for something in 1.19+
     */
    @SuppressWarnings("unchecked")
    default E weSelf()
    {
        return (E) this;
    }

    default void checkUpdateParts()
    {
        // This only does something complex if the parts have changed, otherwise
        // it just ensures their locations are synced to us.
        if (getHolder().holder().tick != weSelf().tickCount)
        {
            getHolder().holder().tick = weSelf().tickCount;
            this.initParts();
        }
    }

    default boolean sameMob(Entity entityIn)
    {
        return this == entityIn || entityIn instanceof PartEntity<?> part && part.getParent() == this;
    }

    void trySubDivideParts(float width, float length, float height);

    default float maxW()
    {
        return 2;
    }

    default float maxH()
    {
        return 2;
    }

    void applyAnimations(IAnimated animHolder);

    default void updatePartsPos()
    {
        this.initParts();
        // check if effective_pose needs updating
        final IAnimated animHolder = ThutCaps.getAnimated(weSelf());
        if (animHolder != null) applyAnimations(animHolder);
        if (getHolder().holder().parts.isEmpty() && getHolder().allParts().isEmpty()) return;

        final Vec3 v = weSelf().position();
        float rotY = weSelf() instanceof LivingEntity e ? e.yBodyRot : weSelf().getYRot();

        // Convert to correct coordinate system and radians
        rotY = 180 - rotY;
        rotY *= Math.PI / 180;

        var transform = getHolder().holder().transform;
        transform.identity();
        transform.translate((float) v.x(), (float) v.y(), (float) v.z());
        transform.rotateY(rotY);
        if(weSelf() instanceof LivingEntity e)
        {
            float scale = e.getScale();
            transform.scale(scale);
        }

        Vector3f r = getHolder().holder().r;
        r.set((float) v.x(), (float) v.y(), (float) v.z());
        final Vec3 dr = new Vec3(r.x - weSelf().xOld, r.y - weSelf().yOld, r.z - weSelf().zOld);
        for (final T p : getUseParts()) p.update(transform, dr);
        if (weSelf().isAddedToLevel() && weSelf().tickCount % 20 == 0) PartSync.sendUpdate(weSelf());
    }
}