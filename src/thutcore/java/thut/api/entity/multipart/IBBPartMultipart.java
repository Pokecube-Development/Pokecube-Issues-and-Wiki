package thut.api.entity.multipart;

import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
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
    BBModel getModel();

    default void applyAnimations(IAnimated animHolder)
    {
        var model = getModel();
        var us = weSelf();
        var partHolder = this.getHolder();
        // Test with once per tick for now, might be
        // able to make it slower for not ridden cases?
        if (partHolder.holder().animTick == us.tickCount) return;

        var holder = AnimationHelper.getHolder(us);
        final List<String> anims = animHolder.getChoices();
        var pose = anims.stream().filter(s -> model.getBuiltInAnimations().containsKey(s))
                .findFirst().orElse("idle");
        partHolder.holder().effective_pose = pose;
        List<Animation> runAnims = model.getBuiltInAnimations().getOrDefault(pose, List.of());
        model.updateAnimation(runAnims, holder);
    }

    default void initFromBBModel()
    {
        var holder = this.getHolder();
        // Sync both lists to each other.
        holder.setParts(holder.allParts());
        holder.clear();
        for (var part : this.getModel().getPartsList())
        {
            if(part instanceof Part p)
            {
                T partEntity = getFactory().create(weSelf(), p);
                holder.allParts().add(partEntity);
            }
        }
    }

    @Override
    default void trySubDivideParts(float width, float length, float height)
    {

    }
}
