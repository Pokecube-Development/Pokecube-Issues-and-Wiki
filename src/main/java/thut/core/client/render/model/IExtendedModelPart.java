package thut.core.client.render.model;

import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.animation.AnimationXML.Mat;

public interface IExtendedModelPart extends IModelCustom
{
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

    Vector4 getDefaultRotations();

    Vector3 getDefaultTranslations();

    String getName();

    IExtendedModelPart getParent();

    int[] getRGBABrO();

    <T> HashMap<String, T> getSubParts();

    String getType();

    default void removeChild(final String name)
    {
        this.getSubParts().remove(name);
    }

    void resetToInit();

    default void setHidden(final boolean hidden)
    {

    }

    default void updateMaterial(final Mat mat)
    {

    }

    void setParent(IExtendedModelPart parent);

    void setPostRotations(Vector4 rotations);

    void setPostTranslations(Vector3 translations);

    void setPreRotations(Vector4 rotations);

    void setPreScale(Vector3 scale);

    void setPreTranslations(Vector3 translations);

    void setRGBABrO(int r, int g, int b, int a, int br, int o);
}
