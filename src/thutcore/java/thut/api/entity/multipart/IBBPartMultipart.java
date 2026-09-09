package thut.api.entity.multipart;

import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated;
import thut.api.entity.animation.Animation;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.client.render.model.parts.Part;

import java.util.List;

public interface IBBPartMultipart<T extends BBPartEntity<E>, E extends Entity> extends IMultpart<T, E>
{
    public static float computeSimpleVolume(Vector3f[] verts)
    {
        var v0 = new Vector3f();
        float v = 0;
        for (int i = 0; i < verts.length; i += 3)
        {
            var v1 = verts[i];
            var v2 = verts[i + 1];
            var v3 = verts[i + 2];
            v += v1.dot(v2.cross(v3, v0)) / 6f;
        }
        return v;
    }

    BBPartEntity.Factory<T, E> getFactory();
    BBModel getBBModel();

    default void applyAnimations(IAnimated animHolder)
    {
        var model = getBBModel();
        if (model == null) return;
        var us = weSelf();
        var partHolder = this.getHolder();
        // Test with once per tick for now, might be
        // able to make it slower for not ridden cases?
        if (partHolder.holder().animTick == us.tickCount) return;

        synchronized (model)
        {
            var holder = AnimationHelper.getHolder(us);
            final List<String> anims = animHolder.getChoices();
            var pose = anims.stream().filter(s -> model.getBuiltInAnimations().containsKey(s)).findFirst()
                    .orElse("idle");
            partHolder.holder().effective_pose = pose;
            for (var p : model.getPartsList()) p.resetToInit();
            holder.setContext(ThutCaps.getAnimated(us));
            List<Animation> runAnims = model.getBuiltInAnimations().getOrDefault(pose, List.of());
            model.updateAnimation(runAnims, holder);
        }
    }

    default void initFromBBModel()
    {
        var holder = this.getHolder();
        // Sync both lists to each other.
        holder.clear();
        holder.setParts(holder.allParts());
        var model = getBBModel();
        var parts = model.getPartsList();
        for (var part : parts)
        {
            part.resetToInit();
            if (part instanceof Part p)
            {
                T partEntity = getFactory().create(weSelf(), p, model);
                if (partEntity.height != 0 && partEntity.width != 0) holder.allParts().add(partEntity);
            }
        }
        holder.holder().animTick = -1;
        final IAnimated animHolder = ThutCaps.getAnimated(weSelf());
        if (animHolder != null) applyAnimations(animHolder);
    }

    @Override
    default void trySubDivideParts(float width, float length, float height)
    {
        // TODO subdivide us here? maybe via a "fake" set of parts?
    }
}
