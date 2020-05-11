package thut.core.client.render.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.entity.Entity;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;

public interface IModel
{
    public static class HeadInfo
    {
        /**
         * This should be updated to match the mob, incase the IModel needs to
         * do custom rendering itself.
         */
        public float headYaw;
        /**
         * This should be updated to match the mob, incase the IModel needs to
         * do custom rendering itself.
         */
        public float headPitch;

        /** This is the current ticksExisted for the object being rendered.. */
        public int currentTick = 0;
        /**
         * This is the ticksExisted before this render tick for the object
         * being rendered
         */
        public int lastTick    = 0;

        public float yawCapMax      = 180;
        public float yawCapMin      = -180;
        public float pitchCapMax    = 40;
        public float pitchCapMin    = -40;
        public int   yawAxis        = 1;
        public int   pitchAxis      = 0;
        public int   yawDirection   = 1;
        public int   pitchDirection = 1;
    }

    public static ImmutableSet<String> emptyAnims = ImmutableSet.of();

    void applyAnimation(Entity entity, IModelRenderer<?> renderer, float partialTicks, float limbSwing);

    default Set<String> getBuiltInAnimations()
    {
        return IModel.emptyAnims;
    }

    HeadInfo getHeadInfo();

    Set<String> getHeadParts();

    HashMap<String, IExtendedModelPart> getParts();

    default void setAnimationHolder(final IAnimationHolder holder)
    {
        this.getParts().forEach((s, p) -> p.setAnimationHolder(holder));
    }

    /**
     * Adjusts for differences in global coordinate systems.
     *
     * @param dy
     */
    default void globalFix(final float dx, final float dy, final float dz)
    {
        // These are the parameters for models exported from blender.
        GlStateManager.rotatef(180, 0, 1, 0);
        GlStateManager.rotatef(90, 1, 0, 0);
        GlStateManager.translatef(0, 0, dy - 1.5f);
    }

    /**
     * @return Whether this model actually exists, if this returns false,
     *         things will often look for a different extension.
     */
    boolean isValid();

    void preProcessAnimations(Collection<List<Animation>> collection);

    default void setHeadInfo(final HeadInfo in)
    {

    }

    default void setOffset(final Vector3 offset)
    {

    }

    default void updateMaterial(final Mat mat)
    {
        for (final IExtendedModelPart part : this.getParts().values())
            part.updateMaterial(mat);
    }
}
