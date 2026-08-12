package thut.core.client.render.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.Entity;
import thut.api.ModelHolder;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector3;
import thut.core.client.render.texturing.IPartTexturer;

public interface IModelRenderer<T extends Entity>
{
    public static final String DEFAULTPHASE = "idle";

    static final Vector3 DEFAULTSCALE = new Vector3().set(1);

    default String getAnimation(final Entity entityIn)
    {
        final IAnimationHolder holder = this.getAnimationHolder();
        if (holder != null) return holder.getAnimation(entityIn);
        return IModelRenderer.DEFAULTPHASE;
    }

    Map<String, List<Animation>> getAnimations();

    default Vector3 getRotationOffset()
    {
        return Vector3.empty;
    }

    default Vector3 getScale()
    {
        return IModelRenderer.DEFAULTSCALE;
    }

    boolean hasAnimation(String phase, Entity entity);

    void scaleEntity(PoseStack mat, Entity entity, IModel model, float partialTick);

    default void setAnimation(final Entity entity, final float partialTick)
    {
        final IAnimationHolder holder = this.getAnimationHolder();
        final String phase = this.getAnimation(entity);
        if (holder != null)
        {
            final List<Animation> anim = this.getAnimations(entity, phase);
            holder.setAnimationChanger(getAnimationChanger());
            if (getAnimations() != null) holder.initAnimations(getAnimations(), IModelRenderer.DEFAULTPHASE);
            if (anim != null && !anim.isEmpty() || (anim != null && phase.equals("none")))
                holder.setPendingAnimations(anim, phase);
        }
    }

    default List<Animation> getAnimations(final Entity entity, final String phase)
    {
        if (this.getAnimations() != null) return this.getAnimations().get(phase);
        return null;
    }

    IAnimationChanger getAnimationChanger();

    IAnimationHolder getAnimationHolder();

    IPartTexturer getTexturer();

    /**
     * This one is only used to store the axis/limits/offsets. For actual
     * rendering, use the one from getAnimationHolder!
     *
     * @return a dummy HeadInfo for loading limits into
     */
    @Nonnull
    HeadInfo getHeadInfo();

    void setAnimationChanger(IAnimationChanger changer);

    void setAnimationHolder(IAnimationHolder holder);

    void setTexturer(IPartTexturer texturer);

    void setRotationOffset(Vector3 offset);

    void setScale(Vector3 scale);

    default void updateModel(ModelHolder model){}
}
