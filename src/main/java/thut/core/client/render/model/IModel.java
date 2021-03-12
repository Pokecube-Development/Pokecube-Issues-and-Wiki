package thut.core.client.render.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Quaternion;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.model.parts.Material;
import thut.core.common.ThutCore;

public interface IModel
{
    public static interface IModelCallback
    {
        void run(IModel model);
    }

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

    Map<String, IExtendedModelPart> getParts();

    default void setAnimationHolder(final IAnimationHolder holder)
    {
        this.getParts().forEach((s, p) -> p.setAnimationHolder(holder));
    }

    /**
     * Adjusts for differences in global coordinate systems.
     *
     * @param mat
     * @param dy
     */
    default void globalFix(final MatrixStack mat, final float dx, final float dy, final float dz)
    {
        // These are the parameters for models exported from blender.
        mat.mulPose(new Quaternion(90, 0, 180, true));
        mat.translate(0, 0, dy - 1.5f);
    }

    /**
     * @return Whether this model actually exists, if this returns false,
     *         things will often look for a different extension.
     */
    boolean isValid();

    default IModel init(final IModelCallback callback)
    {
        if (this.isValid()) callback.run(this);
        return this;
    }

    default boolean isLoaded()
    {
        return true;
    }

    void preProcessAnimations(Collection<List<Animation>> collection);

    default void setHeadInfo(final HeadInfo in)
    {

    }

    default void setOffset(final Vector3 offset)
    {

    }

    default void updateMaterial(final Mat mat)
    {
        mat.name = ThutCore.trim(mat.name);
        final Material material = new Material(mat.name);
        material.diffuseColor = new Vector3f(1, 1, 1);
        material.emissiveColor = new Vector3f(mat.light, mat.light, mat.light);
        material.emissiveMagnitude = Math.min(1, (float) (material.emissiveColor.length() / Math.sqrt(3)) / 0.8f);
        material.specularColor = new Vector3f(1, 1, 1);
        material.alpha = mat.alpha;
        material.flat = !mat.smooth;
        material.transluscent = mat.transluscent;
        for (final IExtendedModelPart part : this.getParts().values())
            part.updateMaterial(mat, material);
    }
}
