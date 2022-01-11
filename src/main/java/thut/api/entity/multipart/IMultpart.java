package thut.api.entity.multipart;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import thut.api.AnimatedCaps;
import thut.api.entity.IAnimated;
import thut.api.entity.multipart.GenericPartEntity.BodyNode;
import thut.api.entity.multipart.GenericPartEntity.BodyPart;
import thut.api.maths.vecmath.Mat3f;
import thut.api.maths.vecmath.Vec3f;
import thut.core.common.ThutCore;

public interface IMultpart<T extends GenericPartEntity<E>, E extends Entity>
{
    public static class Holder<T extends GenericPartEntity<?>>
    {
        public float colWidth = 0;
        public float colHeight = 0;

        public float last_size = 0;

        public T[] allParts;
        public T[] parts;

        public Mat3f rot = new Mat3f();
        public Vec3f r = new Vec3f();
        public String effective_pose = "";

        int tick = -1;
    }

    public static record PartHolder<E extends GenericPartEntity<?>> (List<E> allParts, Map<String, E[]> partMap,
            Holder<E> holder)
    {
        @SuppressWarnings("unchecked")
        public E[] makeAllParts(Class<E> partClass)
        {
            if (holder.allParts == null || holder.allParts.length != allParts.size())
            {
                holder.allParts = (E[]) Array.newInstance(partClass, allParts.size());
                holder.allParts = allParts.toArray(holder.allParts);
            }
            return holder.allParts;
        }

        public E[] getParts()
        {
            return holder.parts;
        }

        public void setParts(E[] parts)
        {
            holder.parts = parts;
        }
    };

    PartHolder<T> getHolder();

    void initParts();

    GenericPartEntity.Factory<T, E> getFactory();

    Class<T> getPartClass();

    @SuppressWarnings("unchecked")
    default E self()
    {
        return (E) this;
    }

    default void checkUpdateParts()
    {
        // This only does something complex if the parts have changed, otherwise
        // it just ensures their locations are synced to us.
        if (getHolder().holder.tick != self().tickCount)
        {
            getHolder().holder.tick = self().tickCount;
            this.initParts();
        }
    }

    default boolean sameMob(Entity entityIn)
    {
        return this == entityIn || entityIn instanceof PartEntity<?> part && part.getParent() == this;
    }

    default T makePart(final BodyPart part, final float size, final Set<String> names)
    {
        final float dx = (float) (part.__pos__.x * size);
        final float dy = (float) (part.__pos__.y * size);
        final float dz = (float) (part.__pos__.z * size);

        final float sx = (float) (part.__size__.x * size);
        final float sy = (float) (part.__size__.y * size);
        final float sz = (float) (part.__size__.z * size);

        final float dw = Math.max(sx, sz);
        final float dh = sy;
        String name = part.name;
        int n = 0;
        while (names.contains(name)) name = part.name + n++;
        return getFactory().create(self(), dw, dh, dx, dy, dz, name);
    }

    default void addPart(final String key, final float size, final BodyNode node)
    {
        try
        {
            @SuppressWarnings("unchecked")
            T[] parts = (T[]) Array.newInstance(getPartClass(), node.parts.size());
            final Set<String> names = Sets.newHashSet();
            for (int i = 0; i < parts.length; i++)
            {
                parts[i] = this.makePart(node.parts.get(i), size, names);
                getHolder().allParts.add(parts[i]);
            }
            getHolder().partMap.put(key, parts);
        }
        catch (NegativeArraySizeException e)
        {
            ThutCore.LOGGER.error(e);
        }
    }

    default void updatePartsPos()
    {
        this.initParts();
        // check if effective_pose needs updating
        final IAnimated animHolder = AnimatedCaps.getAnimated(self());
        if (animHolder != null)
        {
            final List<String> anims = animHolder.getChoices();
            getHolder().holder().effective_pose = "idle";
            for (final String s : anims) if (getHolder().partMap().containsKey(s))
            {
                getHolder().holder().effective_pose = s;
                break;
            }
            // Update the partmap if we know about this pose.
            if (getHolder().partMap().containsKey(getHolder().holder().effective_pose))
                getHolder().setParts(getHolder().partMap().get(getHolder().holder().effective_pose));
        }

        if (getHolder().holder().parts.length == 0 && getHolder().allParts().isEmpty()) return;

        Mat3f rot = getHolder().holder().rot;
        Vec3f r = getHolder().holder().r;

        final Vec3 v = self().position();
        r.set((float) v.x(), (float) v.y(), (float) v.z());
        final Vec3 dr = new Vec3(r.x - self().xOld, r.y - self().yOld, r.z - self().zOld);
        float rotY = self() instanceof LivingEntity e ? e.yBodyRot : self().yRot;

        rot.rotY((float) Math.toRadians(180 - rotY));
        if (self().isAddedToWorld()) for (final T p : getHolder().holder().parts) p.update(rot, r, dr);
        else for (final T p : getHolder().allParts()) p.update(rot, r, dr);
    }
}