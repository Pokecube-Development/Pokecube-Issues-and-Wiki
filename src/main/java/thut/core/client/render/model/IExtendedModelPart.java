package thut.core.client.render.model;

import java.util.HashMap;

import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public interface IExtendedModelPart extends IModelCustom
{
    void addChild(IExtendedModelPart child);

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

    void setParent(IExtendedModelPart parent);

    void setPostRotations(Vector4 rotations);

    void setPostRotations2(Vector4 rotations);

    void setPostTranslations(Vector3 translations);

    void setPreRotations(Vector4 rotations);

    void setPreScale(Vector3 scale);

    void setPreTranslations(Vector3 translations);

    void setRGBABrO(int r, int g, int b, int a, int br, int o);
}
