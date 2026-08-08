package thut.core.client.render.model;

import java.util.Collection;

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

    default void renderAllExcept(final PoseStack mat, final VertexConsumer buffer, final Collection<String> excluded)
    {

    }

    default void renderOnly(final PoseStack mat, final VertexConsumer buffer, final Collection<String> groupNames)
    {

    }

    default void renderPart(final PoseStack mat, final VertexConsumer buffer, final String partName)
    {

    }
}
