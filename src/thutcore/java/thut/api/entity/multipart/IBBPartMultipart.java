package thut.api.entity.multipart;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated;
import thut.api.entity.animation.Animation;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.parts.Part;

import java.util.ArrayList;
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
        var us = weSelf();
        var partHolder = this.getHolder();
        // Test with once per tick for now, might be
        // able to make it slower for not ridden cases?
        if (partHolder.holder().animTick == us.tickCount) return;

        synchronized (model)
        {
            var holder = AnimationHelper.getHolder(us);
            holder.setContext(ThutCaps.getAnimated(us));
            this.setAnimation(us, model, holder);
            partHolder.holder().effective_pose = holder.getAnimation(us);
            for (var p : model.getPartsList()) p.resetToInit();
            final List<Animation> anims = Lists.newArrayList();
            anims.addAll(holder.getTransientPlaying());
            anims.addAll(holder.getPlaying());
            model.updateAnimation(anims, holder);
        }
    }

    default void setAnimation(E entity, IModel model, IAnimated.IAnimationHolder holder)
    {
        var phase = this.getPhase(entity, model);
        var changer = model.getAnimationChanger();

        final List<String> anims = new ArrayList<>();
        changer.getAlternates(anims, entity, phase);
        List<Animation> anim = new ArrayList<>();
        for (final String name : anims)
        {
            var tmp = changer.getAnimations().get(name);
            if (tmp != null) anim.addAll(tmp);
        }
        holder.setAnimationChanger(changer);
        if (changer.getAnimations() != null)
            holder.initAnimations(changer.getAnimations(), IModelRenderer.DEFAULTPHASE);
        if (!anim.isEmpty()) holder.setPendingAnimations(anim, phase);
    }

    default String getPhase(E entity, IModel model)
    {
        final String phase = "idle";
        final IAnimated anims = ThutCaps.getAnimated(entity);
        for (final String s : anims.getChoices()) if (this.hasAnimation(s, model)) return s;
        return phase;
    }

    default boolean hasAnimation(String phase, IModel model)
    {
        var animator = model.getAnimationChanger();
        if (animator != null && animator.hasAnimation(phase)) return true;
        return IModelRenderer.DEFAULTPHASE.equals(phase) || model.getBuiltInAnimations().containsKey(phase);
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
                if (partEntity.height != 0 && partEntity.width != 0)
                {
                    holder.allParts().add(partEntity);
                    partEntity.mod_points.forEach((key, vec) -> {
                        this.getAttachmentPoints().computeIfAbsent(key, k -> new ArrayList<>()).add(vec);
                    });
                }
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
