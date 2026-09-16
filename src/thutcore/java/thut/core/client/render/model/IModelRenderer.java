package thut.core.client.render.model;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.Entity;
import thut.api.ModelHolder;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.core.client.render.texturing.IPartTexturer;

public interface IModelRenderer<T extends Entity>
{
    public static final String DEFAULTPHASE = "idle";

    default String getAnimation(T entityIn, IModel model)
    {
        final IAnimationHolder holder = this.getAnimationHolder();
        if (holder != null) return holder.getAnimation(entityIn);
        return IModelRenderer.DEFAULTPHASE;
    }

    boolean hasAnimation(String phase, T entity, IModel model);

    void scaleEntity(PoseStack mat, T entity, IModel model, float partialTick);

    default void setAnimation(final T entity, final IModel model)
    {
        final IAnimationHolder holder = this.getAnimationHolder();
        final String phase = this.getAnimation(entity, model);
        if (holder != null)
        {
            var changer = model.getAnimationChanger();
            final List<Animation> anim = this.getAnimations(entity, model, phase);
            holder.setAnimationChanger(changer);
            if (changer.getAnimations() != null)
                holder.initAnimations(changer.getAnimations(), IModelRenderer.DEFAULTPHASE);
            if (anim != null && !anim.isEmpty() || (anim != null && phase.equals("none")))
                holder.setPendingAnimations(anim, phase);
        }
    }

    default List<Animation> getAnimations(T entity, IModel model, String phase)
    {
        var changer = model.getAnimationChanger();
        if (changer.getAnimations() != null) return changer.getAnimations().get(phase);
        return null;
    }

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

    void setAnimationHolder(IAnimationHolder holder);

    void setTexturer(IPartTexturer texturer);

    default void updateModel(ModelHolder model){}

    IAnimationChanger getAnimationChanger();
}
