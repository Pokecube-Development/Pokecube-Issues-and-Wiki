package thut.api.entity.multipart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
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

        int tick = -1;

        public void clear()
        {
            allParts = null;
            parts = null;
        }
    }

    public static record PartHolder<E extends GenericPartEntity<?>> (List<E> allParts, Map<String, List<E>> partMap,
            Holder<E> holder)
    {
        public List<E> makeAllParts()
        {
            if (holder.allParts == null || holder.allParts.size() != allParts.size())
            {
                holder.allParts = new ArrayList<>(allParts);
            }
            return holder.allParts;
        }

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

    List<T> getAllParts();

    List<T> getUseParts();

    PartHolder<T> getHolder();

    void initParts();

    GenericPartEntity.Factory<T, E> getFactory();

    /**
     * This is not "self" as forge used that for something in 1.19+
     */
    @SuppressWarnings("unchecked")
    default E weSelf()
    {
        return (E) this;
    }

    void checkUpdateParts();

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

    default void updatePartsPos()
    {
        this.initParts();
        // check if effective_pose needs updating
        final IAnimated animHolder = ThutCaps.getAnimated(weSelf());
        anims:
        if (animHolder != null)
        {
            final List<String> anims = animHolder.getChoices();
            String old_pose = getHolder().holder.effective_pose;
            getHolder().holder().effective_pose = "idle";

            for (final String s : anims) if (getHolder().partMap().containsKey(s))
            {
                getHolder().holder().effective_pose = s;
                break;
            }
            if (old_pose.equals(getHolder().holder().effective_pose)) break anims;
            // Update the partmap if we know about this pose.
            if (getHolder().partMap().containsKey(getHolder().holder().effective_pose))
            {
                getHolder().setParts(getHolder().partMap().get(getHolder().holder().effective_pose));

                boolean subDivided = !getHolder().getParts().isEmpty();

                if (subDivided)
                {
                    float width = Math.min(weSelf().dimensions.width(), maxW());
                    float height = Math.min(weSelf().dimensions.height(), maxH());
                    weSelf().dimensions = EntityDimensions.fixed(width, height);
                    weSelf().noCulling = true;

                    final boolean first = weSelf().firstTick;
                    weSelf().firstTick = true;
                    weSelf().refreshDimensions();
                    weSelf().firstTick = first;
                }
                PartSync.sendUpdate(weSelf());
            }
        }
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

        Vector3f r = getHolder().holder().r;
        r.set((float) v.x(), (float) v.y(), (float) v.z());
        final Vec3 dr = new Vec3(r.x - weSelf().xOld, r.y - weSelf().yOld, r.z - weSelf().zOld);
        if (weSelf().isAddedToLevel())
        {
            for (final T p : getHolder().holder().parts) p.update(transform, dr);
            if (weSelf().tickCount % 20 == 0) PartSync.sendUpdate(weSelf());
        }
        else
        {
            for (final T p : getHolder().allParts()) p.update(transform, dr);
        }
    }
}