package thut.core.client.render.model;

import java.util.HashMap;

import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationXML.Mat;

public interface IExtendedModelPart extends IModelCustom
{
    void addChild(IExtendedModelPart child);

    Vector4 getDefaultRotations();

    Vector3 getDefaultTranslations();

    String getName();

    IExtendedModelPart getParent();

    int[] getRGBAB();

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

    void setPostRotations2(Vector4 rotations);

    void setPostTranslations(Vector3 translations);

    void setPreRotations(Vector4 rotations);

    void setPreScale(Vector3 scale);

    void setPreTranslations(Vector3 translations);

    void setRGBAB(int[] arrays);
}
