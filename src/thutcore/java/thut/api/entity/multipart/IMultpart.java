package thut.api.entity.multipart;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
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
import thut.core.common.network.PartSync;

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

        public void clear()
        {
            allParts = null;
            parts = null;
        }
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

        public void clear()
        {
            allParts.clear();
            partMap.clear();
            holder.clear();
        }
    };

    PartHolder<T> getHolder();

    void initParts();

    GenericPartEntity.Factory<T, E> getFactory();

    Class<T> getPartClass();

    @SuppressWarnings("unchecked")
    /**
     * This is not "self" as forge used that for something in 1.19+
     * 
     * @return
     */
    default E weSelf()
    {
        return (E) this;
    }

    default void checkUpdateParts()
    {
        // This only does something complex if the parts have changed, otherwise
        // it just ensures their locations are synced to us.
        if (getHolder().holder.tick != weSelf().tickCount)
        {
            getHolder().holder.tick = weSelf().tickCount;
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
        return getFactory().create(weSelf(), dw, dh, dx, dy, dz, name);
    }

    default List<T> splitToParts(float width, float height, float length, float x0, float y0, float z0)
    {
        List<T> ret = new ArrayList<T>();
        final int nx = Mth.ceil(width / this.maxW());
        final int nz = Mth.ceil(length / this.maxH());
        final int ny = Mth.ceil(height / this.maxW());

        final float dx = width / nx;
        final float dy = height / ny;
        final float dz = length / nz;

        final float dw = Math.max(width / nx, length / nz);
        final float dh = dy;
        int i = 0;

        for (int y = 0; y < ny; y++) for (int x = 0; x < nx; x++) for (int z = 0; z < nz; z++)
        {
            var part = getFactory().create(weSelf(), dw, dh, x * dx - nx * dx / 2f, y * dy, z * dz - nz * dz / 2f,
                    "part_" + i);
            ret.add(part);
            i++;
        }
        return ret;
    }

    default float maxW()
    {
        return 2;
    }

    default float maxH()
    {
        return 2;
    }

    default void addPart(final String key, final float size, final BodyNode node)
    {
        try
        {
            List<T> list = new ArrayList<T>();
            final Set<String> names = Sets.newHashSet();
            for (int i = 0; i < node.parts.size(); i++)
            {
                var _node = node.parts.get(i);

                float sx = (float) (_node.__size__.x * size);
                float sy = (float) (_node.__size__.y * size);
                float sz = (float) (_node.__size__.z * size);

                if (sx > this.maxW() || sz > this.maxW() || sy > this.maxH())
                {
                    float x0 = (float) (_node.__pos__.x * size);
                    float y0 = (float) (_node.__pos__.y * size);
                    float z0 = (float) (_node.__pos__.z * size);

                    var split = this.splitToParts(sx, sy, sz, x0, y0, z0);
                    for (var part : split)
                    {
                        list.add(part);
                        getHolder().allParts.add(part);
                    }
                }
                else
                {
                    var part = this.makePart(node.parts.get(i), size, names);
                    list.add(part);
                    getHolder().allParts.add(part);
                }
            }
            @SuppressWarnings("unchecked")
            T[] parts = (T[]) Array.newInstance(getPartClass(), list.size());
            getHolder().partMap.put(key, list.toArray(parts));
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
        final IAnimated animHolder = AnimatedCaps.getAnimated(weSelf());
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

                boolean subDivided = getHolder().getParts().length > 0;

                if (subDivided)
                {

                    float width = Math.min(weSelf().dimensions.width, maxW());
                    float height = Math.min(weSelf().dimensions.height, maxH());
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
        if (getHolder().holder().parts.length == 0 && getHolder().allParts().isEmpty()) return;

        Mat3f rot = getHolder().holder().rot;
        Vec3f r = getHolder().holder().r;

        final Vec3 v = weSelf().position();
        r.set((float) v.x(), (float) v.y(), (float) v.z());
        final Vec3 dr = new Vec3(r.x - weSelf().xOld, r.y - weSelf().yOld, r.z - weSelf().zOld);
        float rotY = weSelf() instanceof LivingEntity e ? e.yBodyRot : weSelf().yRot;

        rot.rotY((float) Math.toRadians(180 - rotY));
        if (weSelf().isAddedToWorld())
        {
            for (final T p : getHolder().holder().parts) p.update(rot, r, dr);
            if (weSelf().tickCount % 20 == 0) PartSync.sendUpdate(weSelf());
        }
        else
        {
            for (final T p : getHolder().allParts()) p.update(rot, r, dr);
        }
    }
}