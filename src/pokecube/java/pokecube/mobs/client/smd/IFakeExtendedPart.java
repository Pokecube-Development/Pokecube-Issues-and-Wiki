package pokecube.mobs.client.smd;

import org.joml.Vector3f;
import thut.api.maths.Vector4;
import thut.core.client.render.model.IExtendedModelPart;

/**
 * This is an IExtendedModelPart which defaults most stuff to null or
 * non-existant, this is to be used for things which want to be rendered using
 * other wrappers, but only need the renderAll method.
 */
public interface IFakeExtendedPart extends IExtendedModelPart
{
    @Override
    default void addChild(final IExtendedModelPart child)
    {
    }

    @Override
    String getName();

    @Override
    default IExtendedModelPart getParent()
    {
        return null;
    }

    @Override
    String getType();

    @Override
    default void resetToInit()
    {
    }

    @Override
    default void setParent(final IExtendedModelPart parent)
    {
    }

    @Override
    default void setPostRotations(final Vector4 rotations)
    {
    }

    @Override
    default void setPreRotations(final Vector4 rotations)
    {
    }

    @Override
    default void setPreScale(final Vector3f scale)
    {
    }

    @Override
    default void setPreTranslations(final Vector3f translations)
    {
    }
}
