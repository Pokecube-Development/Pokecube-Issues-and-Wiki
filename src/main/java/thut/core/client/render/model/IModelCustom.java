package thut.core.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public interface IModelCustom
{
    default void renderAll(final MatrixStack mat, final IVertexBuilder buffer)
    {

    }

    default void renderAll(final MatrixStack mat, final IVertexBuilder buffer, final IModelRenderer<?> renderer)
    {
        this.renderAll(mat, buffer, renderer);
    }

    default void renderAllExcept(final MatrixStack mat, final IVertexBuilder buffer, final IModelRenderer<?> renderer,
            final String... excludedGroupNames)
    {
        this.renderAllExcept(mat, buffer, excludedGroupNames);
    }

    default void renderAllExcept(final MatrixStack mat, final IVertexBuilder buffer, final String... excludedGroupNames)
    {

    }

    default void renderOnly(final MatrixStack mat, final IVertexBuilder buffer, final IModelRenderer<?> renderer,
            final String... groupNames)
    {
        this.renderOnly(mat, buffer, groupNames);
    }

    default void renderOnly(final MatrixStack mat, final IVertexBuilder buffer, final String... groupNames)
    {

    }

    default void renderPart(final MatrixStack mat, final IVertexBuilder buffer, final IModelRenderer<?> renderer,
            final String partName)
    {
        this.renderPart(mat, buffer, partName);
    }

    default void renderPart(final MatrixStack mat, final IVertexBuilder buffer, final String partName)
    {

    }
}
