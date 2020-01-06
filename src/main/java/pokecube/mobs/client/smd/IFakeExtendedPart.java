package pokecube.mobs.client.smd;

import java.util.Arrays;
import java.util.HashMap;

import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.IExtendedModelPart;

/**
 * This is an IExtendedModelPart which defaults most stuff to null or
 * non-existant, this is to be used for things which want to be rendered using
 * other wrappers, but only need the renderAll method.
 */
public interface IFakeExtendedPart extends IExtendedModelPart
{
    int[] RGAB = new int[] { 255, 255, 255, 255, 255 };

    @Override
    default void addChild(IExtendedModelPart child)
    {
    }

    @Override
    default Vector4 getDefaultRotations()
    {
        return null;
    }

    @Override
    default Vector3 getDefaultTranslations()
    {
        return null;
    }

    @Override
    String getName();

    @Override
    default IExtendedModelPart getParent()
    {
        return null;
    }

    @Override
    default int[] getRGBAB()
    {
        Arrays.fill(IFakeExtendedPart.RGAB, 255);
        return IFakeExtendedPart.RGAB;
    }

    @SuppressWarnings("unchecked")
    @Override
    HashMap<String, IExtendedModelPart> getSubParts();

    @Override
    String getType();

    @Override
    default void resetToInit()
    {
    }

    @Override
    default void setParent(IExtendedModelPart parent)
    {
    }

    @Override
    default void setPostRotations(Vector4 rotations)
    {
    }

    @Override
    default void setPostRotations2(Vector4 rotations)
    {
    }

    @Override
    default void setPostTranslations(Vector3 translations)
    {
    }

    @Override
    default void setPreRotations(Vector4 rotations)
    {
    }

    @Override
    default void setPreScale(Vector3 scale)
    {
    }

    @Override
    default void setPreTranslations(Vector3 translations)
    {
    }

    @Override
    default void setRGBAB(int[] arrays)
    {
    }
}
