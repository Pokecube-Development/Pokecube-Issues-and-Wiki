package thut.core.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IModelCustom
{
    default void renderAll(final PoseStack mat, final VertexConsumer buffer)
    {

    }

    default void renderAll(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer)
    {
        this.renderAll(mat, buffer, renderer);
    }

    default void renderAllExcept(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer,
            final String... excludedGroupNames)
    {
        this.renderAllExcept(mat, buffer, excludedGroupNames);
    }

    default void renderAllExcept(final PoseStack mat, final VertexConsumer buffer, final String... excludedGroupNames)
    {

    }

    default void renderOnly(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer,
            final String... groupNames)
    {
        this.renderOnly(mat, buffer, groupNames);
    }

    default void renderOnly(final PoseStack mat, final VertexConsumer buffer, final String... groupNames)
    {

    }

    default void renderPart(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer,
            final String partName)
    {
        this.renderPart(mat, buffer, partName);
    }

    default void renderPart(final PoseStack mat, final VertexConsumer buffer, final String partName)
    {

    }
}
