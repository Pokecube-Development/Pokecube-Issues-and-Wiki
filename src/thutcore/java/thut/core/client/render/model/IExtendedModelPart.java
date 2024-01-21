package thut.core.client.render.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel.Holder;

public interface IExtendedModelPart extends IModelCustom
{
    public static interface IPartRenderAdder
    {
        boolean shouldAddTo(IExtendedModelPart part);

        void onRender(PoseStack mat, IExtendedModelPart part);
    }

    public static void sort(final List<IExtendedModelPart> order, final Map<String, IExtendedModelPart> parts)
    {
        order.clear();
        order.addAll(parts.values());
        order.sort((o1, o2) -> {
            boolean transp1 = false;
            boolean transp2 = false;
            for (final Material m : o1.getMaterials())
            {
                if (m == null)
                {
                    continue;
                }
                transp1 = m.transluscent || m.alpha < 1;
                if (transp1) break;
            }
            for (final Material m : o2.getMaterials())
            {
                if (m == null)
                {
                    continue;
                }
                transp2 = m.transluscent || m.alpha < 1;
                if (transp2) break;
            }
            if (transp1 != transp2) return transp1 ? 1 : -1;
            return o1.getName().compareTo(o2.getName());
        });
    }

    void addPartRenderAdder(IPartRenderAdder adder);

    void addChild(IExtendedModelPart child);

    List<Material> getMaterials();

    void applyTexture(MultiBufferSource bufferIn, ResourceLocation tex, IPartTexturer texer);

    default void addMaterial(final Material material)
    {
        this.getMaterials().add(material);
    }

    default void preProcess()
    {
        var parent = this.getParent();
        var child = this;
        child.getRecursiveChildNames().addAll(this.getSubParts().keySet());
        String name = child.getName();
        while (parent != null)
        {
            this.getParentNames().add(parent.getName());
            parent.getRecursiveChildNames().add(name);
            parent.getRecursiveChildNames().addAll(child.getRecursiveChildNames());
            child = parent;
            name = child.getName();
            parent = parent.getParent();
        }
        for (final IExtendedModelPart o : this.getSubParts().values()) o.preProcess();
    }

    default void sort(final List<IExtendedModelPart> order)
    {
        IExtendedModelPart.sort(order, this.getSubParts());
    }

    default Entity convertToGlobal(PoseStack mat, Vector3f fill)
    {
        var holderSup = this.getAnimationHolder();
        if (holderSup == null || holderSup.get() == null) return null;
        if (holderSup.get().getContext() == null) return null;
        if (!(holderSup.get().getContext().getContext() instanceof Entity e)) return null;

        PoseStack mat2 = new PoseStack();
        mat2.last().pose().load(mat.last().pose());
        this.preRender(mat2);

        Vector4f test = new Vector4f(0, 0, 0, 1);
        test.transform(mat2.last().pose());

        // Distance left/right
        double dx = test.x() / 1;
        // Distance up/down, this one is inverted it seems
        double dy = -test.y() / 1;
        // Distance centered
        double dz = test.z() / 1;

        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        // Directions of camera
        var left = camera.getLeftVector();
        var up = camera.getUpVector();
        var fwd = camera.getLookVector();

        var pos = camera.getPosition();

        double x, y, z;
        // Now transform back from camera coordinates
        x = left.x() * dx + up.x() * dy + fwd.x() * dz;
        y = left.y() * dx + up.y() * dy + fwd.y() * dz;
        z = left.z() * dx + up.z() * dy + fwd.z() * dz;

        // And subtract from camera location.
        fill.set((float) (-x + pos.x()), (float) (-y + pos.y()), (float) (-z + pos.z()));

        return e;
    }

    default void preRender(PoseStack mat)
    {

    }

    default void postRender(PoseStack mat)
    {

    }

    Vector3 minBound();

    Vector3 maxBound();

    Vector4 getDefaultRotations();

    Vector3 getDefaultTranslations();

    String getName();

    IExtendedModelPart getParent();

    <T extends IExtendedModelPart> Map<String, T> getSubParts();

    List<IExtendedModelPart> getRenderOrder();

    String getType();

    default void removeChild(final String name)
    {
        this.getSubParts().remove(name);
    }

    void resetToInit();

    default void setHeadPart(final boolean isHead)
    {

    }

    default boolean isHeadPart()
    {
        return false;
    }

    default void setHidden(final boolean hidden)
    {

    }

    default boolean isHidden()
    {
        return false;
    }

    default void setDisabled(final boolean disabled)
    {

    }

    default boolean isDisabled()
    {
        return false;
    }

    default void updateMaterial(final Mat mat, final Material material)
    {

    }

    default Set<String> getParentNames()
    {
        return Sets.newHashSet();
    }

    default Set<String> getRecursiveChildNames()
    {
        return Sets.newHashSet();
    }

    default void setAnimAngles(float rx, float ry, float rz)
    {}

    default void setDefaultAngles(float rx, float ry, float rz)
    {}

    Holder<IAnimationHolder> getAnimationHolder();

    void setAnimationHolder(Holder<IAnimationHolder> input);

    void setParent(IExtendedModelPart parent);

    void setPostRotations(Vector4 rotations);

    void setPostTranslations(Vector3 translations);

    void setPreRotations(Vector4 rotations);

    void setPreScale(Vector3 scale);

    void setPostScale(Vector3 scale);

    void setPreTranslations(Vector3 translations);

    void setColorScales(float r, float g, float b, float a);

    /**
     * Sets the colour for this part
     * 
     * @param material - predicate to check if material is valid
     * @param r
     * @param g
     * @param b
     * @param a
     * @param br
     * @param o
     */
    void setRGBABrO(@Nullable Predicate<Material> material, int r, int g, int b, int a, int br, int o);

    default void setRGBABrO(int r, int g, int b, int a, int br, int o)
    {
        setRGBABrO(m -> true, r, g, b, a, br, o);
    }
}
