package thut.api.entity.multipart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
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

    default void initParts()
    {
        initParts(false);
    }

    Map<String, List<Vector3f>> getAttachmentPoints();

    default boolean shouldSyncParts()
    {
        return true;
    }

    void initParts(boolean fromPacket);

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
        if (this.getUseParts() == null || this.getUseParts().isEmpty()) return;
        var self = weSelf();
        // check if effective_pose needs updating
        final IAnimated animHolder = ThutCaps.getAnimated(self);
        if (animHolder != null) applyAnimations(animHolder);

        final Vec3 v = self.position();
        float rotY = self instanceof LivingEntity e ? e.yBodyRot : self.getYRot();

        // Convert to correct coordinate system and radians
        rotY = 180 - rotY;
        rotY *= Math.PI / 180;

        var transform = getHolder().holder().transform;
        transform.identity();
        transform.translate((float) v.x(), (float) v.y(), (float) v.z());
        transform.rotateY(rotY);
        if(self instanceof LivingEntity e)
        {
            float scale = e.getScale();
            transform.scale(scale);
        }

        Vector3f r = getHolder().holder().r;
        r.set((float) v.x(), (float) v.y(), (float) v.z());
        final Vec3 dr = new Vec3(r.x - self.xOld, r.y - self.yOld, r.z - self.zOld);
        float requiredShift = 1e3f;
        for (final T p : getUseParts())
        {
            p.update(transform);
            float _y = (float) self.getY();
            p.requiredShift = p.r.y - _y;
            requiredShift = Math.min(requiredShift, p.requiredShift);
        }
        // TODO check if this causes any problems
        if(!weSelf().onGround()) requiredShift = 0;
        AABB total = null;
        for (final T p : getUseParts())
        {
            p.requiredShift = requiredShift;
            p.applyPos(dr);
            total = total == null ? p.getBoundingBox() : total.minmax(p.getBoundingBox());
        }
        float dw = (float) Math.max(total.getXsize(), total.getZsize());
        float dh = (float) total.getYsize();
        float dsigma = dw*dh;
        float dsigmaO = self.dimensions.height()*self.dimensions.width();
        float ratio = dsigmaO / dsigma;
        if (Math.abs(ratio > 1 ? ratio : 1 / ratio) > 1.1)
        {
            var dims = EntityDimensions.fixed(dw, dh)
                    .withEyeHeight((float) (total.getYsize() * 0.75));//TODO pull from marker
            self.dimensions = dims;
            if (self instanceof LivingEntity e) e.refreshDimensions();
            self.setBoundingBox(total);
            self.dimensions = dims;
        }
        if (self.isAddedToLevel() && self.tickCount % 20 == 0 && !self.level().isClientSide())
        {
            PartSync.sendUpdate(self);
        }
    }
}