package thut.core.client.render.model;

import java.util.Collection;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IModelCustom
{
    default void render(final PoseStack mat, final VertexConsumer buffer)
    {

    }

    default void renderAll(final PoseStack mat, final VertexConsumer buffer)
    {

    }

    default void renderAll(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer)
    {
        this.renderAll(mat, buffer, renderer);
    }

    default void renderAllExcept(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer,
            final Collection<String> excluded)
    {
        this.renderAllExcept(mat, buffer, excluded);
    }

    default void renderAllExcept(final PoseStack mat, final VertexConsumer buffer, final Collection<String> excluded)
    {

    }

    default void renderOnly(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer,
            final Collection<String> groupNames)
    {
        this.renderOnly(mat, buffer, groupNames);
    }

    default void renderOnly(final PoseStack mat, final VertexConsumer buffer, final Collection<String> groupNames)
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

    default void renderOnly(PoseStack mat, VertexConsumer buf0, String string)
    {
        this.renderOnly(mat, buf0, Sets.newHashSet(string));
    }
}
