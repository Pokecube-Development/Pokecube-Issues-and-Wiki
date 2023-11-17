package thut.core.client.render.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import pokecube.api.PokecubeAPI;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.model.parts.Material;
import thut.core.common.ThutCore;

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

    default void initBuiltInAnimations(@Nullable IModelRenderer<?> renderer, List<Animation> tblAnims)
    {}

    Set<String> getHeadParts();

    Map<String, IExtendedModelPart> getParts();

    List<IExtendedModelPart> getRenderOrder();

    void setAnimationHolder(final IAnimationHolder holder);

    /**
     * Adjusts for differences in global coordinate systems.
     *
     * @param mat
     * @param dy
     */
    default void globalFix(final PoseStack mat, final float dx, final float dy, final float dz)
    {
        // These are the parameters for models exported from blender.
        mat.mulPose(new Quaternion(90, 0, 180, true));
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
        material.expectedTexH = mat.height;
        material.expectedTexW = mat.width;
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
                PokecubeAPI.LOGGER.error(e);
            }
        }
        // Here we loop over the parts values instead of render order, as this
        // is called before the render order is ready to setup. This is also not
        // called during rendering itself, so is fine to be a slower loop.
        for (final IExtendedModelPart part : this.getParts().values()) part.updateMaterial(mat, material);
    }
}
