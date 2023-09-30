package thut.core.client.render.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.model.parts.Material;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;

public interface IModel
{
    public static interface IModelCallback
    {
        void run(IModel model);
    }

    public static ImmutableSet<String> emptyAnims = ImmutableSet.of();

    void applyAnimation(Entity entity, IModelRenderer<?> renderer, float partialTicks, float limbSwing);

    default Set<String> getBuiltInAnimations()
    {
        return IModel.emptyAnims;
    }

    default void initBuiltInAnimations(IModelRenderer<?> renderer, List<Animation> tblAnims)
    {}

    Set<String> getHeadParts();

    Map<String, IExtendedModelPart> getParts();

    List<String> getRenderOrder();

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
    default void globalFix(final PoseStack mat, final float dx, final float dy, final float dz)
    {
        // These are the parameters for models exported from blender.
        mat.mulPose(AxisAngles.MODEL_ROTATE);
        mat.translate(0, 0, dy - 1.5f);
    }

    /**
     * @return Whether this model actually exists, if this returns false, things
     *         will often look for a different extension.
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

    void preProcessAnimations(Collection<Animation> collection);

    default void setOffset(final Vector3 offset)
    {

    }

    default void updateMaterial(final Mat mat)
    {
        mat.name = ThutCore.trim(mat.name);
        final Material material = new Material(mat.name);
        material.diffuseColor = new Vec3f(1, 1, 1);
        material.emissiveColor = new Vec3f(mat.light, mat.light, mat.light);
        material.emissiveMagnitude = Math.min(1, (float) (material.emissiveColor.length() / Math.sqrt(3)) / 0.8f);
        material.specularColor = new Vec3f(1, 1, 1);
        material.alpha = mat.alpha;
        material.flat = !mat.smooth;
        material.transluscent = mat.transluscent;
        material.cull = mat.cull;
        material.shader = mat.shader;
        if (!mat.tex.isBlank())
        {
            try
            {
                material.texture = mat.tex;
                material.tex = new ResourceLocation(mat.tex);
            }
            catch (Exception e)
            {
                ThutCore.LOGGER.error(e);
            }
        }
        for (final IExtendedModelPart part : this.getParts().values()) part.updateMaterial(mat, material);
    }
}
