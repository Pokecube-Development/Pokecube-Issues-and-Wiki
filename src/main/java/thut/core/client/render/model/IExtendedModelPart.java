package thut.core.client.render.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;

public interface IExtendedModelPart extends IModelCustom
{
    public static void sort(final List<String> order, final Map<String, IExtendedModelPart> parts)
    {
        order.clear();
        order.addAll(parts.keySet());
        order.sort((s1, s2) ->
        {
            final IExtendedModelPart o1 = parts.get(s1);
            final IExtendedModelPart o2 = parts.get(s2);
            boolean transp1 = false;
            boolean transp2 = false;
            for (final Material m : o1.getMaterials())
            {
                transp1 = m.transluscent || m.alpha < 1;
                if (transp1) break;
            }
            for (final Material m : o2.getMaterials())
            {
                transp2 = m.transluscent || m.alpha < 1;
                if (transp2) break;
            }
            if (transp1 != transp2) return transp1 ? 1 : -1;
            return s1.compareTo(s2);
        });
    }

    void addChild(IExtendedModelPart child);

    List<Material> getMaterials();

    void applyTexture(IRenderTypeBuffer bufferIn, ResourceLocation tex, IPartTexturer texer);

    default void addMaterial(final Material material)
    {
        this.getMaterials().add(material);
    }

    default void rotateForChild(final MatrixStack mat)
    {

    }

    default void unRotateForChild(final MatrixStack mat)
    {

    }

    default void preProcess()
    {
        for (final IExtendedModelPart o : this.getSubParts().values())
            o.preProcess();
    }

    default void sort(final List<String> order)
    {
        IExtendedModelPart.sort(order, this.getSubParts());
    }

    Vector4 getDefaultRotations();

    Vector3 getDefaultTranslations();

    String getName();

    IExtendedModelPart getParent();

    int[] getRGBABrO();

    <T extends IExtendedModelPart> HashMap<String, T> getSubParts();

    String getType();

    default void removeChild(final String name)
    {
        this.getSubParts().remove(name);
    }

    void resetToInit();

    default void setHidden(final boolean hidden)
    {

    }

    default void updateMaterial(final Mat mat, final Material material)
    {

    }

    void setAnimationHolder(IAnimationHolder holder);

    IAnimationHolder getAnimationHolder();

    void setParent(IExtendedModelPart parent);

    void setPostRotations(Vector4 rotations);

    void setPostTranslations(Vector3 translations);

    void setPreRotations(Vector4 rotations);

    void setPreScale(Vector3 scale);

    void setPreTranslations(Vector3 translations);

    void setRGBABrO(int r, int g, int b, int a, int br, int o);
}
